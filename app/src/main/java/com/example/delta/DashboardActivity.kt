package com.example.delta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                            ReportsActivityScreen(
                                sharedViewModel = sharedViewModel,
                                onHomeClick = {
                                    startActivity(Intent(this, HomePageActivity::class.java))
                                }
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
    var buildings by remember { mutableStateOf<List<Buildings>>(emptyList()) }
    var selectedBuildingId by remember { mutableStateOf<Long?>(null) }
    var showNoDataChartDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = Preference().getUserId(context)
        sharedViewModel.getBuildingsForUser(userId).collect { buildingsList ->
            buildings = buildingsList
            if (selectedBuildingId == null || buildings.none { it.buildingId == selectedBuildingId }) {
                selectedBuildingId = buildings.firstOrNull()?.buildingId
            }
        }
    }

    val selectedBuilding = buildings.find { it.buildingId == selectedBuildingId }
        ?: buildings.firstOrNull()

    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }
    val buildingId = selectedBuilding?.buildingId ?: -1L

    val debtsList by sharedViewModel.getDebtsForBuilding(buildingId).collectAsState(initial = emptyList())
    val paysList by sharedViewModel.getPaysForBuilding(buildingId).collectAsState(initial = emptyList())

    val excludedDescriptions = setOf("شارژ", "رهن", "اجاره")

    val debtsByCostName = debtsList
        .filter { it.description !in excludedDescriptions }
        .groupBy { it.description }
        .mapValues { it.value.sumOf { it.amount } }

    val paysByCostName = paysList
        .filter { it.description !in excludedDescriptions }
        .groupBy { it.description }
        .mapValues { it.value.sumOf { it.amount } }


    val unitsList by sharedViewModel.getUnitsForBuilding(buildingId).collectAsState(initial = emptyList())
    val ownersList by sharedViewModel.getOwnersForBuilding(buildingId).collectAsState(initial = emptyList())

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val allCostNames = (debtsByCostName.keys + paysByCostName.keys).distinct()
    val xAxisLabels = allCostNames.toTypedArray()

    LaunchedEffect(Unit) {
        val userId = Preference().getUserId(context)
        sharedViewModel.getBuildingsForUser(userId).collect { buildingsList ->
            buildings = buildingsList
            if (buildings.isNotEmpty()) {
                // Check debts and pays for ALL buildings in parallel or sequentially
                var foundBuildingWithDataId: Long? = null

                // Sequential for simplicity (can optimize with coroutines)
                for (building in buildings) {
                    val debts = sharedViewModel.getDebtsForBuilding(building.buildingId).first()
                    val pays = sharedViewModel.getPaysForBuilding(building.buildingId).first()
                    if (debts.isNotEmpty() || pays.isNotEmpty()) {
                        foundBuildingWithDataId = building.buildingId
                        break
                    }
                }

                if (foundBuildingWithDataId != null) {
                    selectedBuildingId = foundBuildingWithDataId
                    showNoDataChartDialog = false
                } else {
                    // No building has debts or pays
                    selectedBuildingId = buildings.first().buildingId
                    showNoDataChartDialog = true
                }
            } else {
                // No buildings at all
                selectedBuildingId = null
                showNoDataChartDialog = true
            }
        }
    }

// Get debts and pays filtered by selectedCategory (cost description)
    val filteredDebtsByUnit = remember(debtsList, selectedCategory) {
        if (selectedCategory == null) emptyMap<String, Double>()
        else debtsList
            .filter { it.description == selectedCategory }
            .groupBy { debt ->
                unitsList.find { it.unitId == debt.unitId }?.unitNumber ?: context.getString(R.string.other)
            }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val filteredPaysByUnit = remember(paysList, selectedCategory) {
        if (selectedCategory == null) emptyMap<String, Double>()
        else paysList
            .filter { it.description == selectedCategory }
            .groupBy { pay ->
                unitsList.find { it.unitId == pay.unitId }?.unitNumber ?: context.getString(R.string.other)
            }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

// X-axis labels for detail chart are unit numbers associated to the selectedCategory
    val detailXAxisLabels = (filteredDebtsByUnit.keys + filteredPaysByUnit.keys).distinct().toTypedArray()

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
                            label = { Text(building.name, style = MaterialTheme.typography.bodyLarge) },
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
                                // labelRotationAngle = -45f
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
                            val debtDataSet = BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                                color = Color(context.getColor(R.color.Red_light)).toArgb()
                                valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                                valueTextSize = 12f
                                valueTypeface = yekanTypeface
                                valueFormatter = MoneyValueFormatter()
                            }
                            val payDataSet = BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
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
                            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                                if (e == null) return
                                val xIndex = e.x.toInt()
                                selectedCategory = if (xIndex in allCostNames.indices) {
                                    allCostNames[xIndex]
                                } else {
                                    null
                                }
                            }

                            override fun onNothingSelected() {
                                selectedCategory = null
                            }
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(350.dp).padding(horizontal = 16.dp)
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
                    // Pass sums for the selected category now
                    CategoryDetailChartByUnit(
                        category = selectedCategory!!,
                        debtsByUnit = filteredDebtsByUnit,
                        paysByUnit = filteredPaysByUnit,
                        xAxisLabels = detailXAxisLabels,
                        modifier = Modifier.weight(1f).padding(16.dp)
                    )


                }
            }
        }
    }
    if (showNoDataChartDialog) {
        AlertDialog(
            onDismissRequest = { showNoDataChartDialog = false },
            title = {
                Text(text = context.getString(R.string.chart_alarm), style = MaterialTheme.typography.bodyLarge)
            },
            text = {
                Text(text = context.getString(R.string.chart_alarm_info), style = MaterialTheme.typography.bodyLarge)
            },
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
            text = "${context.getString(R.string.cost_detail)} $category",
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
            modifier = Modifier.fillMaxWidth().height(350.dp)
        )
    }
}


// Modified CategoryDetailChart accepts sum amounts instead of list of Debts
@Composable
fun CategoryDetailChart(
    category: String,
    debtsSum: Double,
    paysSum: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }
    val xAxisLabels = arrayOf(category)

    Column(modifier = modifier) {
        Text(
            text = "${context.getString(R.string.cost_detail)} $category",
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
                val debtsEntries = listOf(BarEntry(0f, debtsSum.toFloat()))
                val paysEntries = listOf(BarEntry(0f, paysSum.toFloat()))

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
            },
            modifier = Modifier.fillMaxWidth().height(350.dp)
        )
    }
}

// You might already have this; only included to avoid compile errors:
class MoneyValueFormatter : ValueFormatter() {
    private val formatter = NumberFormat.getNumberInstance(Locale.US)

    override fun getFormattedValue(value: Float): String {
        // Format float as number with commas, ignoring fraction digits
        return formatter.format(value.toDouble())
    }
}
