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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.example.delta.data.entity.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.delta.init.Preference

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
    // Hold the buildings list in state, initially empty
    var buildings by remember { mutableStateOf<List<Buildings>>(emptyList()) }

    // Hold the selected building ID in state
    // Initialize with -1L or null and update once buildings loaded
    var selectedBuildingId by remember { mutableStateOf<Long?>(null) }
    // Launch coroutine to fetch user's buildings once on entry
    var showNoDataChartDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = Preference().getUserId(context = context)
        sharedViewModel.getBuildingsForUser(userId).collect { buildingsList ->
            buildings = buildingsList
            // When buildings load, if selectedBuildingId is not set or invalid, select first building
            if (selectedBuildingId == null || buildings.none { it.buildingId == selectedBuildingId }) {
                selectedBuildingId = buildings.firstOrNull()?.buildingId
            }
        }
    }



    val selectedBuilding = buildings.find { it.buildingId == selectedBuildingId }
        ?: buildings.firstOrNull()

    val yekanTypeface = remember {
        ResourcesCompat.getFont(context, R.font.yekan)
    }

    val buildingId = selectedBuilding?.buildingId ?: -1L

    // Collect data for charts
    val debtsList by sharedViewModel.getDebtsForBuilding(buildingId).collectAsState(initial = emptyList())
    val paysList by sharedViewModel.getPaysForBuilding(buildingId).collectAsState(initial = emptyList())
    val unitsList by sharedViewModel.getUnitsForBuilding(buildingId).collectAsState(initial = emptyList())
    val ownersList by sharedViewModel.getOwnersForBuilding(buildingId).collectAsState(initial = emptyList())

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val debtsByCategory = debtsList.groupBy { it.description }
    val paysByCategory = paysList.groupBy { it.description }
    val allCategories = (debtsByCategory.keys + paysByCategory.keys).distinct()
    val xAxisLabels = allCategories.toTypedArray()

    LaunchedEffect(debtsList, paysList, buildings) {
        if (debtsList.isEmpty() && paysList.isEmpty()) {
            showNoDataChartDialog = true
        } else {
            showNoDataChartDialog = false
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onHomeClick
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(context.getString(R.string.title_dashboard), style = MaterialTheme.typography.bodyLarge) }
            )
        }
    ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Building selector row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = context.getString(R.string.bilan_report),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                            textColor = Color(context.getColor(R.color.black)).toArgb()
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            setDrawGridLines(false)
                            typeface = yekanTypeface
                            labelRotationAngle = -45f
                            setCenterAxisLabels(true)
                            valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                        }
                    }
                },
                update = { chart ->
                    val debtsEntries = ArrayList<BarEntry>()
                    val paysEntries = ArrayList<BarEntry>()

                    allCategories.forEachIndexed { index, category ->
                        val debtAmount = debtsByCategory[category]?.sumOf { it.amount }?.toFloat() ?: 0f
                        val payAmount = paysByCategory[category]?.sumOf { it.amount }?.toFloat() ?: 0f
                        debtsEntries.add(BarEntry(index.toFloat(), debtAmount))
                        paysEntries.add(BarEntry(index.toFloat(), payAmount))
                    }

                    if (debtsEntries.isNotEmpty() || paysEntries.isNotEmpty()) {
                        val debtDataSet = BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                            color = Color(context.getColor(R.color.Red_light)).toArgb()
                            valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                            valueTextSize = 12f
                            valueTypeface = yekanTypeface
                        }
                        val payDataSet = BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
                            color = Color(context.getColor(R.color.Green)).toArgb()
                            valueTextColor = Color(context.getColor(R.color.black)).toArgb()
                            valueTextSize = 12f
                            valueTypeface = yekanTypeface
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
                            selectedCategory = if (xIndex in allCategories.indices) {
                                allCategories[xIndex]
                            } else {
                                null
                            }
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

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedCategory == null) {
                Text(
                    text = context.getString(R.string.click_on_cost_columns),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // Show detailed category chart
                CategoryDetailChart(
                    category = selectedCategory!!,
                    debts = debtsByCategory[selectedCategory] ?: emptyList(),
                    pays = paysByCategory[selectedCategory] ?: emptyList(),
                    unitsList = unitsList,
                    ownersList = ownersList,
                    modifier = Modifier.weight(1f).padding(16.dp)
                )
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
                        context.getString(R.string.confirm),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }
}

@Composable
fun CategoryDetailChart(
    category: String,
    debts: List<Debts>,
    pays: List<Debts>,
    unitsList: List<Units>,
    ownersList: List<Owners>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val yekanTypeface = remember { ResourcesCompat.getFont(context, R.font.yekan) }
    val unitsMap = remember(unitsList) { unitsList.associateBy { it.unitId } }
    val ownersMap = remember(ownersList) { ownersList.associateBy { it.ownerId } }

    fun labelForDebt(debt: Debts): String {
        return debt.unitId?.let { unitsMap[it]?.unitNumber }
            ?: debt.ownerId?.let { ownerId ->
                ownersMap[ownerId]?.let { "${it.firstName} ${it.lastName}" }
            } ?: context.getString(R.string.other)
    }

    val debtsByLabel = debts.groupBy { labelForDebt(it) }
    val paysByLabel = pays.groupBy { labelForDebt(it) }
    val allLabels = (debtsByLabel.keys + paysByLabel.keys).distinct()
    val xAxisLabels = allLabels.toTypedArray()

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
                        labelRotationAngle = -45f
                        setCenterAxisLabels(true)
                        textSize = 14f
                        typeface = yekanTypeface
                    }
                }
            },
            update = { chart ->
                val debtsEntries = mutableListOf<BarEntry>()
                val paysEntries = mutableListOf<BarEntry>()
                allLabels.forEachIndexed { index, label ->
                    val debtSum = debtsByLabel[label]?.sumOf { it.amount }?.toFloat() ?: 0f
                    val paySum = paysByLabel[label]?.sumOf { it.amount }?.toFloat() ?: 0f
                    debtsEntries.add(BarEntry(index.toFloat(), debtSum))
                    paysEntries.add(BarEntry(index.toFloat(), paySum))
                }

                if (debtsEntries.isNotEmpty() || paysEntries.isNotEmpty()) {
                    val debtDataSet = BarDataSet(debtsEntries, context.getString(R.string.debt)).apply {
                        color = Color(getColor(R.color.Red_light)).toArgb()
                        valueTextColor = Color(getColor(R.color.black)).toArgb()
                        valueTextSize = 12f
                        valueTypeface = yekanTypeface
                    }
                    val payDataSet = BarDataSet(paysEntries, context.getString(R.string.payments)).apply {
                        color = Color(getColor(R.color.Green)).toArgb()
                        valueTextColor = Color(getColor(R.color.black)).toArgb()
                        valueTextSize = 12f
                        valueTypeface = yekanTypeface
                    }
                    val data = BarData(debtDataSet, payDataSet).apply { barWidth = 0.3f }
                    val groupSpace = 0.4f
                    val barSpace = 0.05f

                    data.groupBars(0f, groupSpace, barSpace)
                    chart.data = data
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                    chart.xAxis.axisMaximum = chart.data.xMax + groupSpace + 0.9f
                    chart.xAxis.axisMinimum = -0.5f
                    chart.xAxis.typeface = yekanTypeface
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

// You might already have this; only included to avoid compile errors:
class MoneyValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        // Format float as currency or desired format, e.g.:
        return String.format("%.0f", value)
    }
}
