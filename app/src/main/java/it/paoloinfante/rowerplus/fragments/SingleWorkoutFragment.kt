package it.paoloinfante.rowerplus.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.ColorFormatter
import com.github.mikephil.charting.renderer.LineChartRenderer
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.chart.HrZonesFillRenderer
import it.paoloinfante.rowerplus.chart.PieChartValueFormatter
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import it.paoloinfante.rowerplus.repositories.PreferencesRepository
import it.paoloinfante.rowerplus.utils.HeartRateZones
import it.paoloinfante.rowerplus.utils.WorkoutPerformanceDataBuilder
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SingleWorkoutFragment : Fragment(R.layout.fragment_single_workout) {
    private lateinit var distanceChart: LineChart
    private lateinit var rpmChart: LineChart
    private lateinit var caloriesChart: LineChart
    private lateinit var bpmChart: LineChart

    private lateinit var hrZonesLabel: TextView
    private lateinit var hrZonesChart: PieChart

    private var workoutData: WorkoutWithStatuses? = null

    private var textColor: Int = 0
    private lateinit var chartSeriesColors: IntArray
    private lateinit var hrZonesColors: IntArray

    private val workoutDataViewViewModel by activityViewModels<WorkoutDataViewViewModel>()

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val args: SingleWorkoutFragmentArgs by navArgs()
    private var hrZones: HeartRateZones? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textColorTypedValues =
            requireContext().theme.obtainStyledAttributes(intArrayOf(R.attr.textColor))
        textColor = textColorTypedValues.getColor(0, Color.BLACK)
        textColorTypedValues.recycle()

        setHasOptionsMenu(true)

        chartSeriesColors = resources.getIntArray(R.array.chartSeriesColors)
        hrZonesColors = resources.getIntArray(R.array.hrZonesColors)

        val userAge = preferencesRepository.userAge
        if (userAge != null) {
            hrZones = HeartRateZones(
                requireContext(),
                hrZonesColors.foldIndexed(EnumMap(HeartRateZones.Zones::class.java)) { index, out, color ->
                    when (index) {
                        0 -> out[HeartRateZones.Zones.LOW_INTENSITY] = color
                        1 -> out[HeartRateZones.Zones.ZONE_1] = color
                        2 -> out[HeartRateZones.Zones.ZONE_2] = color
                        3 -> out[HeartRateZones.Zones.ZONE_3] = color
                        4 -> out[HeartRateZones.Zones.ZONE_4] = color
                        5 -> out[HeartRateZones.Zones.ZONE_5] = color
                        6 -> out[HeartRateZones.Zones.CRITICAL] = color
                    }
                    out
                },
                userAge
            )
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        distanceChart = view.findViewById(R.id.distanceChart)
        applyStyleToChart(distanceChart)

        rpmChart = view.findViewById(R.id.rpmChart)
        applyStyleToChart(rpmChart)

        caloriesChart = view.findViewById(R.id.caloriesChart)
        applyStyleToChart(caloriesChart)

        bpmChart = view.findViewById(R.id.bpmChart)
        applyStyleToChart(bpmChart)

        hrZonesLabel = view.findViewById(R.id.hrZonesLabel)
        hrZonesChart = view.findViewById(R.id.hrZonesChart)

        hrZonesChart.description.text = ""
        hrZonesChart.setCenterTextColor(textColor)
        hrZonesChart.setNoDataTextColor(textColor)
        hrZonesChart.legend.textColor = textColor

        lifecycleScope.launch {
            workoutDataViewViewModel.getWorkout(args.workoutId).collect(workoutCollector)
        }
    }

    private fun applyStyleToChart(chart: LineChart) {
        chart.description.text = ""
        chart.axisLeft.textColor = textColor
        chart.xAxis.textColor = textColor
        chart.legend.textColor = textColor
    }

    private val workoutCollector = FlowCollector<WorkoutWithStatuses?> { workout ->
        workoutData = workout

        if (workout != null) {
            val performanceDataBuilder = WorkoutPerformanceDataBuilder(workout.workoutStatuses)

            // Distance
            val distanceDataset = LineDataSet(
                performanceDataBuilder.buildDistanceChart(), getString(
                    R.string.performance_distance_chart_series
                )
            )
            distanceDataset.color = chartSeriesColors[0]
            distanceDataset.valueTextColor = textColor
            distanceDataset.setDrawFilled(true)
            distanceDataset.fillColor = chartSeriesColors[0]
            distanceChart.data = LineData(distanceDataset)
            distanceChart.invalidate()

            // RPM
            val rpmDataset = LineDataSet(
                performanceDataBuilder.buildRPMChart(),
                getString(R.string.performance_rpm_chart_series)
            )
            rpmDataset.color = chartSeriesColors[1]
            rpmDataset.valueTextColor = textColor
            rpmChart.data = LineData(rpmDataset)
            rpmChart.invalidate()

            // Calories
            val caloriesDataset = LineDataSet(
                performanceDataBuilder.buildCaloriesChart(), getString(
                    R.string.performance_calories_chart_series
                )
            )
            caloriesDataset.color = chartSeriesColors[2]
            caloriesDataset.valueTextColor = textColor
            caloriesDataset.setDrawFilled(true)
            caloriesDataset.fillColor = chartSeriesColors[2]
            caloriesChart.data = LineData(caloriesDataset)
            caloriesChart.invalidate()

            // BPM
            val bpmEntries = performanceDataBuilder.buildBPMChart()
            val bpmDataset = LineDataSet(
                bpmEntries, getString(
                    R.string.performance_bpm_chart_series
                )
            )
            bpmDataset.color = chartSeriesColors[3]
            bpmDataset.valueTextColor = textColor
            bpmDataset.fillColor = chartSeriesColors[3]
            bpmDataset.setDrawFilled(true)
            bpmChart.data = LineData(bpmDataset)
            if (hrZones != null) {
                bpmDataset.fillAlpha = 135
                bpmChart.renderer = HrZonesFillRenderer(
                    hrZones!!,
                    bpmChart,
                    bpmChart.animator,
                    bpmChart.viewPortHandler
                )
            } else {
                bpmChart.renderer =
                    LineChartRenderer(bpmChart, bpmChart.animator, bpmChart.viewPortHandler)
            }
            bpmChart.invalidate()

            if (hrZones != null) {
                // HR Zones
                hrZonesLabel.visibility = View.VISIBLE
                hrZonesChart.visibility = View.VISIBLE

                val hrZonesDataset = PieDataSet(hrZones!!.getPieData(bpmEntries), "")
                hrZonesDataset.valueTextColor = textColor
                hrZonesDataset.colors = hrZonesColors.toList()
                hrZonesDataset.setDrawIcons(false)
                hrZonesDataset.valueTextSize = 16f
                hrZonesDataset.valueFormatter = PieChartValueFormatter()
                hrZonesChart.data = PieData(hrZonesDataset)
                hrZonesChart.setUsePercentValues(true)
                hrZonesChart.setDrawCenterText(false)
                hrZonesChart.setHoleColor(Color.TRANSPARENT)
                hrZonesChart.setDrawEntryLabels(false)
                hrZonesChart.invalidate()
            } else {
                hrZonesLabel.visibility = View.GONE
                hrZonesChart.visibility = View.GONE
            }
        }
    }

    val hrColorFormatter = ColorFormatter { index, e, set ->
        when (hrZones?.getZone(e.y.roundToInt()) ?: HeartRateZones.Zones.LOW_INTENSITY) {
            HeartRateZones.Zones.LOW_INTENSITY -> hrZonesColors[0]
            HeartRateZones.Zones.ZONE_1 -> hrZonesColors[1]
            HeartRateZones.Zones.ZONE_2 -> hrZonesColors[2]
            HeartRateZones.Zones.ZONE_3 -> hrZonesColors[3]
            HeartRateZones.Zones.ZONE_4 -> hrZonesColors[4]
            HeartRateZones.Zones.ZONE_5 -> hrZonesColors[5]
            HeartRateZones.Zones.CRITICAL -> hrZonesColors[6]
        }
    }

    private fun exportToExcelSpreadsheet() {
        if (workoutData == null) {
            Toast.makeText(requireContext(), "No data available", Toast.LENGTH_SHORT).show()
            return
        }

        val workbook = XSSFWorkbook()
        val workSheet = workbook.createSheet("Rower Data")

        val firstRow = workSheet.createRow(0)

        firstRow.createCell(0).setCellValue("Row Number")
        firstRow.createCell(1).setCellValue("Time Elapsed (seconds)")
        firstRow.createCell(2).setCellValue("Distance (meters)")
        firstRow.createCell(3).setCellValue("Calories (kcal)")
        firstRow.createCell(4).setCellValue("Rows per Minute")
        firstRow.createCell(5).setCellValue("Time (seconds) for 500m")

        workoutData!!.workoutStatuses.forEachIndexed { index, workoutStatus ->
            val row = workSheet.createRow(index + 1)
            row.createCell(0).setCellValue(workoutStatus.rowsCount.toDouble())
            row.createCell(1).setCellValue(workoutStatus.timeElapsed.toDouble())
            row.createCell(2).setCellValue(workoutStatus.distance.toDouble())
            row.createCell(3).setCellValue(workoutStatus.calories.toDouble())
            row.createCell(4).setCellValue(workoutStatus.currentRPM.toDouble())
            row.createCell(5)
                .setCellValue(workoutStatus.currentSecsFor500M.toDouble())
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy.HH.mm.ss", Locale.ITALY)

        try {
            val outFile = File.createTempFile(
                "rower_data_${dateFormat.format(workoutData!!.workout.time)}_",
                ".xlsx",
                requireContext().cacheDir
            )
            outFile.createNewFile()

            val fileOs = outFile.outputStream()
            workbook.write(fileOs)
            fileOs.close()

            val fileShareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "rowerplus-${dateFormat.format(workoutData!!.workout.time)}.xlsx"
                )
                val fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().packageName}.provider",
                    outFile
                )
                putExtra(
                    Intent.EXTRA_STREAM,
                    fileUri
                )
            }

            startActivity(Intent.createChooser(fileShareIntent, null))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                getString(R.string.error_createfile),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.single_workout, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun askDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.workout_delete_confirmation_message)
            .setNegativeButton(R.string.message_no) { _, _ ->

            }
            .setPositiveButton(
                R.string.message_yes
            ) { _, _ ->
                lifecycleScope.launch {
                    workoutDataViewViewModel.deleteWorkoutById(args.workoutId)
                    findNavController().navigateUp()
                }
            }.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_single -> {
                askDelete()
                true
            }
            R.id.export_xlsx -> {
                exportToExcelSpreadsheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}