package com.example.delta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.example.delta.data.entity.Costs
import com.example.delta.enums.DashboardCategory
import com.example.delta.enums.FundType
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.CapitalOwnerBreakdown
import com.example.delta.volley.Cost
import com.example.delta.volley.Cost.BuildingWithCosts
import com.example.delta.volley.UnitBreakdown
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.forEachIndexed

class DashboardActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        ReportsActivityScreen(
                            sharedViewModel = sharedViewModel,
                            onHomeClick = { startActivity(Intent(this, HomePageActivity::class.java)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsActivityScreen(
    sharedViewModel: SharedViewModel,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current

    var selectedBuildingId by remember { mutableStateOf<Long?>(null) }
    var showNoDataChartDialog by remember { mutableStateOf(false) }

    var buildingsWithCosts by remember { mutableStateOf<List<BuildingWithCosts>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Cost().fetchBuildingsWithCosts(
            context = context,
            mobileNumber = Preference().getUserMobile(context) ?: "",
            onSuccess = { list ->
                buildingsWithCosts = list
                isLoading = false

                if (list.isNotEmpty()) {
                    if (selectedBuildingId == null) {
                        selectedBuildingId = list.first().building.buildingId
                    }
                    showNoDataChartDialog = false
                } else {
                    selectedBuildingId = null
                    showNoDataChartDialog = true
                }
            },
            onError = { e ->
                errorMessage = e.message
                isLoading = false
                showNoDataChartDialog = true
            }
        )
    }

    val buildings = remember(buildingsWithCosts) {
        buildingsWithCosts.map { it.building }
    }

    LaunchedEffect(selectedBuildingId) {
        val bId = selectedBuildingId
        if (bId != null && bId > 0) {
            sharedViewModel.loadDashboard(context, bId)
        }
    }

    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }

    val debtsList by sharedViewModel.dashboardDebts.collectAsState()
    val paysList by sharedViewModel.dashboardPays.collectAsState()
    val unitsList by sharedViewModel.dashboardUnits.collectAsState()
    val receiptList by sharedViewModel.dashboardReceipt.collectAsState()
    val pendingReceiptList by sharedViewModel.dashboardPendingReceipt.collectAsState()
    val costsList by sharedViewModel.dashboardCosts.collectAsState()
    val capitalSummary by sharedViewModel.dashboardCapitalSummary.collectAsState()
    val chargeSummary by sharedViewModel.dashboardChargeSummary.collectAsState()
    val operationalSummary by sharedViewModel.dashboardOperationalSummary.collectAsState()

    val capitalDetailByOwner by sharedViewModel.dashboardCapitalDetailByOwner.collectAsState()
    val chargeDetailByUnit by sharedViewModel.dashboardChargeDetailByUnit.collectAsState()
    val operationalDetailByUnit by sharedViewModel.dashboardOperationalDetailByUnit.collectAsState()
    var showNoRecordedData by remember { mutableStateOf(false) }
    showNoRecordedData = debtsList.isEmpty() && paysList.isEmpty()

    val excludedDescriptions = setOf(
        context.getString(R.string.charge),
        context.getString(R.string.mortgage),
        context.getString(R.string.rent)
    )

    val costsById = remember(costsList) {
        costsList.associateBy { it.costId }
    }

    val operationalEffectiveCostIds = remember(costsList) {
        costsList
            .filter { it.fundType == FundType.OPERATIONAL }
            .groupBy { it.costName }
            .filter { (_, group) -> group.any { it.tempAmount == 0.0 } }
            .flatMap { (_, group) -> group.map { it.costId } }
            .toSet()
    }

    fun resolveCategory(cost: Costs): CostCategoryInfo? {
        if (cost.costName in excludedDescriptions) return null
        return if (cost.fundType == FundType.CAPITAL) {
            CostCategoryInfo(
                key = "CAPITAL_${cost.costId}",
                title = cost.costName,
                fundType = cost.fundType
            )
        } else {
            val isEffective = operationalEffectiveCostIds.contains(cost.costId)
            if (isEffective) {
                CostCategoryInfo(
                    key = "OP_EFF_${cost.costId}",
                    title = cost.costName,
                    fundType = cost.fundType
                )
            } else {
                CostCategoryInfo(
                    key = "OP_CURRENT",
                    title = context.getString(R.string.current_costs),
                    fundType = cost.fundType
                )
            }
        }
    }

    data class CategoryAgg(
        val info: CostCategoryInfo,
        var debtTotal: Double,
        var payTotal: Double
    )

    val categoryAggMap = remember(debtsList, paysList, costsList) {
        val map = linkedMapOf<String, CategoryAgg>()

        fun ensureCategory(cost: Costs): CategoryAgg? {
            val info = resolveCategory(cost) ?: return null
            val existing = map[info.key]
            if (existing != null) return existing
            val agg = CategoryAgg(info, 0.0, 0.0)
            map[info.key] = agg
            return agg
        }

        debtsList.forEach { d ->
            val cost = costsById[d.costId] ?: return@forEach
            val agg = ensureCategory(cost) ?: return@forEach
            agg.debtTotal += d.amount
        }

        paysList.forEach { p ->
            val cost = costsById[p.costId] ?: return@forEach
            val agg = ensureCategory(cost) ?: return@forEach
            agg.payTotal += p.amount
        }

        map
    }

    val allCategories = categoryAggMap.values.map { it.info }
    val xAxisLabels = allCategories.map { it.title }.toTypedArray()

    val debtsByCategoryKey = categoryAggMap.mapValues { it.value.debtTotal }
    val paysByCategoryKey = categoryAggMap.mapValues { it.value.payTotal }

    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedCategoryKey) {
        if (selectedCategoryKey != null) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }


    val filteredDebtsByUnit = remember(debtsList, selectedCategoryKey, unitsList, costsList) {
        if (selectedCategoryKey == null) emptyMap<String, Double>()
        else {
            debtsList
                .filter { d ->
                    val cost = costsById[d.costId] ?: return@filter false
                    val cat = resolveCategory(cost) ?: return@filter false
                    cat.key == selectedCategoryKey
                }
                .groupBy { debt ->
                    unitsList.find { it.unitId == debt.unitId }?.unitNumber
                        ?: context.getString(R.string.other)
                }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }

    val filteredPaysByUnit = remember(paysList, selectedCategoryKey, unitsList, costsList) {
        if (selectedCategoryKey == null) emptyMap<String, Double>()
        else {
            paysList
                .filter { p ->
                    val cost = costsById[p.costId] ?: return@filter false
                    val cat = resolveCategory(cost) ?: return@filter false
                    cat.key == selectedCategoryKey
                }
                .groupBy { pay ->
                    unitsList.find { it.unitId == pay.unitId }?.unitNumber
                        ?: context.getString(R.string.other)
                }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
    }

    val detailXAxisLabels = (filteredDebtsByUnit.keys + filteredPaysByUnit.keys)
        .distinct()
        .toTypedArray()


    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    fun extractYear(date: String?): String? {
        if (date.isNullOrBlank()) return null
        val parts = date.split("/")
        return if (parts.isNotEmpty()) parts[0] else null
    }

    val currentYear: String = remember {
        val pc = PersianCalendar()
        pc.persianYear.toString()
    }


    val totalDebtsCurrentYear: Double = remember(debtsList, currentYear) {
        debtsList
            .filter { extractYear(it.dueDate) == currentYear }
            .sumOf { it.amount / 1_000_000f }
    }

    val totalPaysCurrentYear: Double = remember(paysList, currentYear) {
        paysList
            .filter { extractYear(it.dueDate) == currentYear }
            .sumOf { it.amount / 1_000_000f}
    }

    val totalReceiptCurrentYear: Double = remember(receiptList, currentYear) {
        receiptList
            .filter { extractYear(it.dueDate) == currentYear }
            .sumOf { it.amount / 1_000_000f}
    }

    val totalPendingReceiptCurrentYear: Double = remember(pendingReceiptList, currentYear) {
        pendingReceiptList
            .filter { extractYear(it.dueDate) == currentYear }
            .sumOf { it.amount / 1_000_000f}
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onHomeClick) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.title_dashboard),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            if (!showNoDataChartDialog) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    items(buildings) { building ->
                        FilterChip(
                            selected = building.buildingId == selectedBuildingId,
                            onClick = { selectedBuildingId = building.buildingId },
                            label = {
                                Text(
                                    building.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Text(
                    text = context.getString(R.string.bilan_report),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = context.getString(R.string.current_year_debt),
                        subtitle = currentYear?.let { "سال $it" } ?: "",
                        amount = totalDebtsCurrentYear,
                        formatter = numberFormatter,
                        modifier = Modifier.weight(1f),
                        amountColor = Color(context.getColor(R.color.Red_light))
                    )
                    SummaryCard(
                        title = context.getString(R.string.current_year_pays),
                        subtitle = currentYear?.let { "سال $it" } ?: "",
                        amount = totalPaysCurrentYear,
                        formatter = numberFormatter,
                        modifier = Modifier.weight(1f),
                        amountColor = Color(context.getColor(R.color.Green))
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = context.getString(R.string.current_year_pending_receipt),
                        subtitle = currentYear?.let { "سال $it" } ?: "",
                        amount = totalPendingReceiptCurrentYear,
                        formatter = numberFormatter,
                        modifier = Modifier.weight(1f),
                        amountColor = Color(context.getColor(R.color.Red_light))
                    )
                    SummaryCard(
                        title = context.getString(R.string.current_year_receipt),
                        subtitle = currentYear?.let { "سال $it" } ?: "",
                        amount = totalReceiptCurrentYear,
                        formatter = numberFormatter,
                        modifier = Modifier.weight(1f),
                        amountColor = Color(context.getColor(R.color.Green))
                    )
                }

                if (showNoRecordedData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = context.getString(R.string.no_data_recorded),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                setNoDataText(context.getString(R.string.no_data))
                                description.isEnabled = false
                                setPinchZoom(false)
                                setDrawGridBackground(false)
                                legend.isEnabled = true
                                animateY(1000)
                                axisRight.isEnabled = false
                                axisLeft.apply {
                                    axisMinimum = 0f
                                    setDrawGridLines(true)
                                    granularity = 1f
                                    typeface = yekanTypeface
                                    valueFormatter = MillionValueFormatter()
                                    textSize = 13f
                                }
                                legend.apply {
                                    typeface = yekanTypeface
                                    textSize = 12f
                                    textColor = Color(ctx.getColor(R.color.black)).toArgb()
                                }
                                xAxis.apply {
                                    position = XAxis.XAxisPosition.BOTTOM
                                    granularity = 1f
                                    setDrawGridLines(false)
                                    typeface = yekanTypeface
                                    labelRotationAngle = -15f
                                    setCenterAxisLabels(true)
                                }
                            }
                        },
                        update = { chart ->
                            val labels = arrayOf("هزینه های عمرانی", "هزینه های جاری", "شارژ")

                            val debtsEntries = mutableListOf<BarEntry>()
                            val paysEntries = mutableListOf<BarEntry>()

                            val capitalDebt = capitalSummary!!.unpaid.toFloat()
                            val capitalPay = capitalSummary!!.paid.toFloat()
                            val operationalDebt = operationalSummary!!.unpaid.toFloat()
                            val operationalPay = operationalSummary!!.paid.toFloat()
                            val chargeDebt = chargeSummary!!.unpaid.toFloat()
                            val chargePay = chargeSummary!!.paid.toFloat()

                            debtsEntries.add(BarEntry(0f, capitalDebt))
                            paysEntries.add(BarEntry(0f, capitalPay))

                            debtsEntries.add(BarEntry(1f, operationalDebt))
                            paysEntries.add(BarEntry(1f, operationalPay))

                            debtsEntries.add(BarEntry(2f, chargeDebt))
                            paysEntries.add(BarEntry(2f, chargePay))

                            val hasData = debtsEntries.any { it.y != 0f } || paysEntries.any { it.y != 0f }

                            if (hasData) {
                                val debtDataSet = BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                                    color = Color(context.getColor(R.color.Red_light)).toArgb()
                                    valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                                    valueTextSize = 12f
                                    valueTypeface = yekanTypeface
                                    valueFormatter = MillionValueFormatter()
                                }
                                val payDataSet = BarDataSet(
                                    paysEntries,
                                    context.getString(R.string.payments)
                                ).apply {
                                    color = Color(context.getColor(R.color.Green)).toArgb()
                                    valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                                    valueTextSize = 12f
                                    valueTypeface = yekanTypeface
                                    valueFormatter = MillionValueFormatter()
                                }

                                val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
                                val groupSpace = 0.4f
                                val barSpace = 0.05f

                                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                                chart.data = data
                                data.groupBars(0f, groupSpace, barSpace)
                                chart.xAxis.axisMinimum = -0.5f
                                chart.xAxis.axisMaximum = data.xMax + groupSpace + 0.9f
                                chart.invalidate()
                            } else {
                                chart.clear()
                                chart.invalidate()
                            }

                            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                                override fun onValueSelected(
                                    e: com.github.mikephil.charting.data.Entry?,
                                    h: Highlight?
                                ) {
                                    if (e == null) return
                                    val xIndex = e.x.toInt()
                                    selectedCategoryKey = when (xIndex) {
                                        0 -> "هزینه های عمرانی"
                                        1 -> "هزینه های جاری"
                                        2 -> "شارژ"
                                        else -> null
                                    }
                                }

                                override fun onNothingSelected() {
                                    selectedCategoryKey = null
                                }
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(horizontal = 16.dp)
                    )


                    Spacer(Modifier.height(16.dp))

                    Spacer(Modifier.height(16.dp))

                    if (selectedCategoryKey == null) {
                        Text(
                            text = context.getString(R.string.click_on_cost_columns),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        when (selectedCategoryKey) {
                            "هزینه های عمرانی" -> {
                                CategoryDetailChartByOwner(
                                    category = "هزینه های عمرانی",
                                    items = capitalDetailByOwner,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                            "هزینه های جاری" -> {
                                CategoryDetailChartByUnit(
                                    category = "هزینه های جاری",
                                    items = operationalDetailByUnit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                            "شارژ" -> {
                                CategoryDetailChartByUnit(
                                    category = "شارژ",
                                    items = chargeDetailByUnit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    if (showNoDataChartDialog) {
        AlertDialog(
            onDismissRequest = { showNoDataChartDialog = false },
            title = {
                Text(
                    text = context.getString(R.string.chart_alarm),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    text = context.getString(R.string.chart_alarm_info),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoDataChartDialog = false
                        onHomeClick()
                    }
                ) {
                    Text(
                        text = context.getString(R.string.confirm),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }
}

fun formatMillionLabel(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val million = value / 1_000_000.0
    return formatter.format(million) + " میلیون تومان"
}


@Composable
fun CategoryDetailChartByUnit(
    category: String,
    items: List<UnitBreakdown>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }

    val labels = items.map { it.unitNumber.ifBlank { it.unitId.toString() } }.toTypedArray()

    Column(modifier = modifier) {
        Text(
            text = "${context.getString(R.string.cost_detail)} $category ${context.getString(R.string.based_on_unit)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        AndroidView(
            factory = { ctx ->
                BarChart(ctx).apply {
                    setNoDataText(context.getString(R.string.no_data))
                    description.isEnabled = false
                    setPinchZoom(false)
                    setDrawGridBackground(false)
                    legend.isEnabled = true
                    axisRight.isEnabled = false
                    axisLeft.apply {
                        axisMinimum = 0f
                        setDrawGridLines(true)
                        granularity = 1f
                        typeface = yekanTypeface
                        valueFormatter = MillionValueFormatter()
                    }
                    legend.apply {
                        typeface = yekanTypeface
                        textSize = 12f
                        textColor = Color(ctx.getColor(R.color.black)).toArgb()
                    }
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        setDrawGridLines(false)
                        setCenterAxisLabels(true)
                        textSize = 14f
                        typeface = yekanTypeface
                        valueFormatter = IndexAxisValueFormatter(labels)
                    }
                }
            },
            update = { chart ->
                val debtsEntries = mutableListOf<BarEntry>()
                val paysEntries = mutableListOf<BarEntry>()

                items.forEachIndexed { index, item ->
                    debtsEntries.add(BarEntry(index.toFloat(), item.unpaid.toFloat()))
                    paysEntries.add(BarEntry(index.toFloat(), item.paid.toFloat()))
                }

                val hasData = debtsEntries.any { it.y != 0f } || paysEntries.any { it.y != 0f }

                if (hasData) {
                    val debtDataSet =
                        BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                            color = Color(context.getColor(R.color.secondary_color)).toArgb()
                            valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                            valueTextSize = 12f
                            valueTypeface = yekanTypeface
                            valueFormatter = MillionValueFormatter()
                        }
                    val payDataSet =
                        BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
                            color = Color(context.getColor(R.color.teal_700)).toArgb()
                            valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                            valueTextSize = 12f
                            valueTypeface = yekanTypeface
                            valueFormatter = MillionValueFormatter()
                        }

                    val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
                    val groupSpace = 0.4f
                    val barSpace = 0.05f

                    data.groupBars(0f, groupSpace, barSpace)
                    chart.data = data
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.xAxis.axisMaximum = data.xMax + groupSpace + 0.9f
                    chart.xAxis.axisMinimum = -0.5f
                    chart.notifyDataSetChanged()
                    chart.animateY(1000)
                    chart.invalidate()
                } else {
                    chart.clear()
                    chart.invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
    }
}


@Composable
fun CategoryDetailChartByOwner(
    category: String,
    items: List<CapitalOwnerBreakdown>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }

    val labels = items.map { it.fullName.ifBlank { it.ownerId.toString() } }.toTypedArray()

    Column(modifier = modifier) {
        Text(
            text = "${context.getString(R.string.cost_detail)} $category ${context.getString(R.string.based_on_unit)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        AndroidView(
            factory = { ctx ->
                BarChart(ctx).apply {
                    description.isEnabled = false
                    setPinchZoom(false)
                    setDrawGridBackground(false)
                    legend.isEnabled = true
                    axisRight.isEnabled = false
                    axisLeft.apply {
                        axisMinimum = 0f
                        setDrawGridLines(true)
                        granularity = 1f
                        typeface = yekanTypeface
                        valueFormatter = MillionValueFormatter()
                    }
                    legend.apply {
                        typeface = yekanTypeface
                        textSize = 12f
                        textColor = Color(ctx.getColor(R.color.black)).toArgb()
                    }
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        setDrawGridLines(false)
                        setCenterAxisLabels(true)
                        textSize = 14f
                        typeface = yekanTypeface
                        valueFormatter = IndexAxisValueFormatter(labels)
                    }
                }
            },
            update = { chart ->
                val debtsEntries = mutableListOf<BarEntry>()
                val paysEntries = mutableListOf<BarEntry>()

                items.forEachIndexed { index, item ->
                    debtsEntries.add(BarEntry(index.toFloat(), item.unpaid.toFloat()))
                    paysEntries.add(BarEntry(index.toFloat(), item.paid.toFloat()))
                }

                val hasData = debtsEntries.any { it.y != 0f } || paysEntries.any { it.y != 0f }

                if (hasData) {
                    val debtDataSet =
                        BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                            color = Color(context.getColor(R.color.secondary_color)).toArgb()
                            valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                            valueTextSize = 12f
                            valueTypeface = yekanTypeface
                            valueFormatter = MillionValueFormatter()
                        }
                    val payDataSet =
                        BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
                            color = Color(context.getColor(R.color.teal_700)).toArgb()
                            valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                            valueTextSize = 12f
                            valueTypeface = yekanTypeface
                            valueFormatter = MillionValueFormatter()
                        }

                    val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
                    val groupSpace = 0.4f
                    val barSpace = 0.05f

                    data.groupBars(0f, groupSpace, barSpace)
                    chart.data = data
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.xAxis.axisMaximum = data.xMax + groupSpace + 0.9f
                    chart.xAxis.axisMinimum = -0.5f
                    chart.notifyDataSetChanged()
                    chart.animateY(1000)
                    chart.invalidate()
                } else {
                    chart.clear()
                    chart.invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
    }
}


@Composable
fun SummaryCard(
    title: String,
    subtitle: String,
    amount: Double,
    formatter: NumberFormat,
    modifier: Modifier = Modifier,
    amountColor: Color = Color.Black
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = formatter.format(amount),
                style = MaterialTheme.typography.titleMedium,
                color = amountColor
            )
        }
    }
}

data class CostCategoryInfo(
    val key: String,
    val title: String,
    val fundType: FundType
)


class MoneyValueFormatter : ValueFormatter() {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)
    override fun getFormattedValue(value: Float): String = formatter.format(value.toDouble())
}

class MillionValueFormatter : ValueFormatter() {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)
    override fun getFormattedValue(value: Float): String {
        val million = value / 1_000_000f
        return formatter.format(million.toDouble())
    }
}

