package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.CareLogEntity
import com.example.data.GrowthPhotoEntity
import com.example.data.PlantEntity
import com.example.data.PlantNoteEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generatePlantJsonReport(
        plant: PlantEntity,
        careLogs: List<CareLogEntity>,
        growthPhotos: List<GrowthPhotoEntity>,
        notes: List<PlantNoteEntity>
    ): String {
        val root = JSONObject()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val plantObj = JSONObject().apply {
            put("id", plant.id)
            put("name", plant.name)
            put("species", plant.species)
            put("room", plant.room)
            put("waterIntervalDays", plant.waterIntervalDays)
            put("fertilizeIntervalDays", plant.fertilizeIntervalDays)
            put("mistIntervalDays", plant.mistIntervalDays)
            put("seasonPreset", plant.seasonPresetName)
            put("climateSetting", plant.climateSettingName)
            put("notes", plant.notes)
            put("createdAt", sdf.format(Date(plant.createdAt)))
        }
        root.put("plant_profile", plantObj)

        val logsArr = JSONArray()
        careLogs.forEach { log ->
            logsArr.put(JSONObject().apply {
                put("id", log.id)
                put("careType", log.careType)
                put("timestamp", sdf.format(Date(log.timestamp)))
                put("note", log.note ?: "")
            })
        }
        root.put("care_logs", logsArr)

        val photosArr = JSONArray()
        growthPhotos.forEach { photo ->
            photosArr.put(JSONObject().apply {
                put("id", photo.id)
                put("imagePath", photo.imagePath)
                put("timestamp", sdf.format(Date(photo.timestamp)))
                put("note", photo.note ?: "")
            })
        }
        root.put("growth_photos", photosArr)

        val notesArr = JSONArray()
        notes.forEach { note ->
            notesArr.put(JSONObject().apply {
                put("id", note.id)
                put("text", note.text)
                put("timestamp", sdf.format(Date(note.timestamp)))
            })
        }
        root.put("notes", notesArr)

        return root.toString(2)
    }

    fun generateHtmlReport(
        plant: PlantEntity,
        careLogs: List<CareLogEntity>,
        growthPhotos: List<GrowthPhotoEntity>,
        notes: List<PlantNoteEntity>
    ): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val logsRows = careLogs.joinToString("") { log ->
            """
            <tr>
                <td style="padding: 8px; border-bottom: 1px solid #ddd;">${sdf.format(Date(log.timestamp))}</td>
                <td style="padding: 8px; border-bottom: 1px solid #ddd; font-weight: bold;">${log.careType}</td>
                <td style="padding: 8px; border-bottom: 1px solid #ddd;">${log.note ?: "-"}</td>
            </tr>
            """.trimIndent()
        }

        val photoCards = growthPhotos.joinToString("") { photo ->
            """
            <div style="margin-bottom: 16px; padding: 12px; border: 1px solid #eee; border-radius: 8px; background: #fafafa;">
                <p style="font-weight: bold; margin: 0 0 4px 0;">📸 ${sdf.format(Date(photo.timestamp))}</p>
                <p style="margin: 0; color: #555;">${photo.note ?: "No note recorded."}</p>
            </div>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Plant Pulse Report - ${plant.name}</title>
            <style>
                body { font-family: sans-serif; padding: 24px; color: #2d3748; line-height: 1.5; }
                h1 { color: #1b4d3e; margin-bottom: 4px; }
                .subtitle { color: #718096; font-size: 14px; margin-bottom: 24px; }
                .card { background: #f7fafc; padding: 16px; border-radius: 12px; border: 1px solid #e2e8f0; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                th { text-align: left; background: #e2e8f0; padding: 8px; font-size: 14px; }
            </style>
        </head>
        <body>
            <h1>🪴 ${plant.name}</h1>
            <div class="subtitle">Species: ${plant.species.ifEmpty { "Indoor Plant" }} | Room: ${plant.room}</div>
            
            <div class="card">
                <h3>📋 Care Specs & Schedule</h3>
                <p><b>Watering Frequency:</b> Every ${plant.waterIntervalDays} days</p>
                <p><b>Fertilizing Frequency:</b> Every ${plant.fertilizeIntervalDays} days</p>
                <p><b>Misting Frequency:</b> Every ${plant.mistIntervalDays} days</p>
                <p><b>Season Preset:</b> ${plant.seasonPresetName}</p>
                <p><b>Microclimate:</b> ${plant.climateSettingName}</p>
                <p><b>Notes:</b> ${plant.notes.ifEmpty { "None" }}</p>
            </div>

            <div class="card">
                <h3>📸 Growth Scrapbook Timeline (${growthPhotos.size} Photos)</h3>
                $photoCards
            </div>

            <div class="card">
                <h3>💧 Care History Log (${careLogs.size} Records)</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Action</th>
                            <th>Note</th>
                        </tr>
                    </thead>
                    <tbody>
                        $logsRows
                    </tbody>
                </table>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printOrExportPdf(
        context: Context,
        plant: PlantEntity,
        careLogs: List<CareLogEntity>,
        growthPhotos: List<GrowthPhotoEntity>,
        notes: List<PlantNoteEntity>
    ) {
        val htmlContent = generateHtmlReport(plant, careLogs, growthPhotos, notes)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("${plant.name}_Report")
                val jobName = "PlantPulse Report - ${plant.name}"
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    fun shareJsonReport(context: Context, jsonStr: String, plantName: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, jsonStr)
            type = "application/json"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share JSON Report for $plantName")
        context.startActivity(shareIntent)
    }
}
