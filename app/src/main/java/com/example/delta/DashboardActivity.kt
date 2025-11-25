package com.example.delta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.delta.data.entity.Buildings
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.BuildingWithCosts
import com.example.delta.volley.Cost
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.first

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

    // 1) Load buildings + costs from server (single request)
    LaunchedEffect(Unit) {
        Cost().fetchBuildingsWithCosts(
            context = context,
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

    // 2) Flatten buildings list from buildingsWithCosts
    val buildings = remember(buildingsWithCosts) {
        buildingsWithCosts.map { it.building }
    }

    // 3) When selected building changes, load dashboard data (debts, pays, units, owners, …)
    LaunchedEffect(selectedBuildingId) {
        val bId = selectedBuildingId
        if (bId != null && bId > 0) {
            sharedViewModel.loadDashboard(context, bId)
        }
    }

    // 4) Resolve selected building object
    val selectedBuilding = buildings.find { it.buildingId == selectedBuildingId }
        ?: buildings.firstOrNull()

    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }
    val buildingId = selectedBuilding?.buildingId ?: -1L

    val debtsList by sharedViewModel.dashboardDebts.collectAsState()
    val paysList by sharedViewModel.dashboardPays.collectAsState()
    val allCosts by sharedViewModel.dashboardCosts.collectAsState()
    val unitsList by sharedViewModel.dashboardUnits.collectAsState()
    val ownersList by sharedViewModel.dashboardOwners.collectAsState()

    var showNoRecordedData by remember { mutableStateOf(false) }
    showNoRecordedData = debtsList.isEmpty() && paysList.isEmpty()

    val excludedDescriptions = setOf(
        context.getString(R.string.charge),
        context.getString(R.string.mortgage),
        context.getString(R.string.rent)
    )

    val debtsByCostName = debtsList
        .filter { it.description !in excludedDescriptions }
        .groupBy { it.description }
        .mapValues { it.value.sumOf { d -> d.amount } }

    val paysByCostName = paysList
        .filter { it.description !in excludedDescriptions }
        .groupBy { it.description }
        .mapValues { it.value.sumOf { p -> p.amount } }

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val allCostNames = (debtsByCostName.keys + paysByCostName.keys).distinct()
    val xAxisLabels = allCostNames.toTypedArray()

    val filteredDebtsByUnit = remember(debtsList, selectedCategory, unitsList) {
        if (selectedCategory == null) emptyMap<String, Double>()
        else debtsList
            .filter { it.description == selectedCategory }
            .groupBy { debt ->
                unitsList.find { it.unitId == debt.unitId }?.unitNumber
                    ?: context.getString(R.string.other)
            }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val filteredPaysByUnit = remember(paysList, selectedCategory, unitsList) {
        if (selectedCategory == null) emptyMap<String, Double>()
        else paysList
            .filter { it.description == selectedCategory }
            .groupBy { pay ->
                unitsList.find { it.unitId == pay.unitId }?.unitNumber
                    ?: context.getString(R.string.other)
            }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val detailXAxisLabels = (filteredDebtsByUnit.keys + filteredPaysByUnit.keys)
        .distinct()
        .toTypedArray()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onHomeClick) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = context.getString(R.string.title_dashboard), style = MaterialTheme.typography.bodyLarge) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!showNoDataChartDialog) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = context.getString(R.string.bilan_report),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (showNoRecordedData) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                    valueFormatter = MoneyValueFormatter()
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
                                    valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                                }
                            }
                        },
                        update = { chart ->
                            val debtsEntries = mutableListOf<BarEntry>()
                            val paysEntries = mutableListOf<BarEntry>()

                            allCostNames.forEachIndexed { index, costName ->
                                val debtAmount = debtsByCostName[costName]?.toFloat() ?: 0f
                                val payAmount = paysByCostName[costName]?.toFloat() ?: 0f
                                debtsEntries.add(BarEntry(index.toFloat(), debtAmount))
                                paysEntries.add(BarEntry(index.toFloat(), payAmount))
                            }

                            if (debtsEntries.isNotEmpty() || paysEntries.isNotEmpty()) {
                                val debtDataSet =
                                    BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                                        color = Color(context.getColor(R.color.Red_light)).toArgb()
                                        valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                                        valueTextSize = 12f
                                        valueTypeface = yekanTypeface
                                        valueFormatter = MoneyValueFormatter()
                                    }
                                val payDataSet = BarDataSet(
                                    paysEntries,
                                    context.getString(R.string.payments)
                                ).apply {
                                    color = Color(context.getColor(R.color.Green)).toArgb()
                                    valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                                    valueTextSize = 12f
                                    valueTypeface = yekanTypeface
                                    valueFormatter = MoneyValueFormatter()
                                }

                                val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
                                val groupSpace = 0.4f
                                val barSpace = 0.05f

                                data.groupBars(0f, groupSpace, barSpace)
                                chart.data = data
                                chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                                chart.xAxis.axisMaximum = data.xMax + groupSpace + 0.9f
                                chart.xAxis.axisMinimum = -0.5f
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
                                    selectedCategory = if (xIndex in allCostNames.indices) {
                                        allCostNames[xIndex]
                                    } else null
                                }

                                override fun onNothingSelected() {
                                    selectedCategory = null
                                }
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    if (selectedCategory == null) {
                        Text(
                            text = context.getString(R.string.click_on_cost_columns),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        CategoryDetailChartByUnit(
                            category = selectedCategory!!,
                            debtsByUnit = filteredDebtsByUnit,
                            paysByUnit = filteredPaysByUnit,
                            xAxisLabels = detailXAxisLabels,
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showNoDataChartDialog) {
        AlertDialog(
            onDismissRequest = { showNoDataChartDialog = false },
            title = { Text(text = context.getString(R.string.chart_alarm), style = MaterialTheme.typography.bodyLarge) },
            text = { Text(text = context.getString(R.string.chart_alarm_info), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoDataChartDialog = false
                        onHomeClick()
                    }
                ) {
                    Text(text = context.getString(R.string.confirm), style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }
}

@Composable
fun CategoryDetailChartByUnit(
    category: String,
    debtsByUnit: Map<String, Double>,
    paysByUnit: Map<String, Double>,
    xAxisLabels: Array<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }

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
                        valueFormatter = MoneyValueFormatter()
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
                        valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                    }
                }
            },
            update = { chart ->
                val debtsEntries = mutableListOf<BarEntry>()
                val paysEntries = mutableListOf<BarEntry>()

                xAxisLabels.forEachIndexed { index, unitLabel ->
                    val debtSum = debtsByUnit[unitLabel]?.toFloat() ?: 0f
                    val paySum = paysByUnit[unitLabel]?.toFloat() ?: 0f
                    debtsEntries.add(BarEntry(index.toFloat(), debtSum))
                    paysEntries.add(BarEntry(index.toFloat(), paySum))
                }

                if (debtsEntries.isNotEmpty() || paysEntries.isNotEmpty()) {
                    val debtDataSet = BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                        color = Color(context.getColor(R.color.secondary_color)).toArgb()
                        valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                        valueTextSize = 12f
                        valueTypeface = yekanTypeface
                        valueFormatter = MoneyValueFormatter()
                    }
                    val payDataSet = BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
                        color = Color(context.getColor(R.color.teal_700)).toArgb()
                        valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                        valueTextSize = 12f
                        valueTypeface = yekanTypeface
                        valueFormatter = MoneyValueFormatter()
                    }

                    val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
                    val groupSpace = 0.4f
                    val barSpace = 0.05f

                    data.groupBars(0f, groupSpace, barSpace)
                    chart.data = data
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
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

//@Composable
//fun CategoryDetailChart(
//    category: String,
//    debtsSum: Double,
//    paysSum: Double,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }
//    val xAxisLabels = arrayOf(category)
//
//    Column(modifier = modifier) {
//        Text(
//            text = "${context.getString(R.string.cost_detail)} $category",
//            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.padding(bottom = 12.dp)
//        )
//        AndroidView(
//            factory = { ctx ->
//                BarChart(ctx).apply {
//                    description.isEnabled = false
//                    setPinchZoom(false)
//                    setDrawGridBackground(false)
//                    legend.isEnabled = true
//                    axisRight.isEnabled = false
//                    axisLeft.apply {
//                        axisMinimum = 0f
//                        setDrawGridLines(true)
//                        granularity = 1f
//                        typeface = yekanTypeface
//                        valueFormatter = MoneyValueFormatter()
//                    }
//                    legend.apply {
//                        typeface = yekanTypeface
//                        textSize = 12f
//                        textColor = Color(ctx.getColor(R.color.black)).toArgb()
//                    }
//                    xAxis.apply {
//                        position = XAxis.XAxisPosition.BOTTOM
//                        granularity = 1f
//                        setDrawGridLines(false)
//                        setCenterAxisLabels(true)
//                        textSize = 14f
//                        typeface = yekanTypeface
//                        valueFormatter = IndexAxisValueFormatter(xAxisLabels)
//                    }
//                }
//            },
//            update = { chart ->
//                val debtsEntries = listOf(BarEntry(0f, debtsSum.toFloat()))
//                val paysEntries = listOf(BarEntry(0f, paysSum.toFloat()))
//
//                val debtDataSet = BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
//                    color = Color(context.getColor(R.color.secondary_color)).toArgb()
//                    valueTextColor = Color(context.getColor(R.color.black)).toArgb()
//                    valueTextSize = 12f
//                    valueTypeface = yekanTypeface
//                    valueFormatter = MoneyValueFormatter()
//                }
//
//                val payDataSet = BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
//                    color = Color(context.getColor(R.color.teal_700)).toArgb()
//                    valueTextColor = Color(context.getColor(R.color.black)).toArgb()
//                    valueTextSize = 12f
//                    valueTypeface = yekanTypeface
//                    valueFormatter = MoneyValueFormatter()
//                }
//
//                val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
//                val groupSpace = 0.4f
//                val barSpace = 0.05f
//
//                data.groupBars(0f, groupSpace, barSpace)
//                chart.data = data
//                chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
//                chart.xAxis.axisMaximum = data.xMax + groupSpace + 0.9f
//                chart.xAxis.axisMinimum = -0.5f
//                chart.notifyDataSetChanged()
//                chart.animateY(1000)
//                chart.invalidate()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(350.dp)
//        )
//    }
//}

class MoneyValueFormatter : ValueFormatter() {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)
    override fun getFormattedValue(value: Float): String = formatter.format(value.toDouble())
}
