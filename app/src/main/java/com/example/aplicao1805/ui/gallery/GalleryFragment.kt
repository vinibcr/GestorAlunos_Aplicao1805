package com.example.aplicao1805.ui.gallery

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.aplicao1805.R
import com.example.aplicao1805.databinding.FragmentGalleryBinding
import com.example.aplicao1805.ui.home.UrlManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class CalendarEdit(val accountId: String, val presence: Boolean, val date: String, val dayOfWeek: String)

data class TopicEdit(val accountId: String, val date: String, val topic: String)

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarView: CalendarView
    private lateinit var buttonPresence: Button
    private lateinit var buttonAbsence: Button
    private lateinit var checkboxTopic: CheckBox
    private val calendarEdits: MutableList<CalendarEdit> = mutableListOf()

    private val sdf = SimpleDateFormat("dd/MM/yyyy")
    private val sdfJson = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfDayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault())
    private var selectedDate = Calendar.getInstance() // Variável para armazenar a data selecionada

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val rootView: View = binding.root

        calendarView = binding.calendarView
        buttonPresence = binding.buttonPresence
        buttonAbsence = binding.buttonAbsence
        checkboxTopic = rootView.findViewById(R.id.checkboxTopic)

        val interactionListener = View.OnClickListener { view ->
            val isPresence = view == buttonPresence

            if (checkboxTopic.isChecked) { // Verifica se a caixa de seleção está marcada
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(if (isPresence) "Presença" else "Falta")
                builder.setMessage("Deseja registrar o tópico da matéria?")

                val input = EditText(requireContext())
                val container = LinearLayout(requireContext())
                container.orientation = LinearLayout.VERTICAL
                container.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                container.addView(input)
                builder.setView(container)

                builder.setPositiveButton("OK") { dialog, _ ->
                    val topic = input.text.toString().toUpperCase(Locale.getDefault())
                    if (topic.isNotEmpty()) {
                        // data selecionada:
                        sendTopicEditToAPI(selectedDate.formatDateForJson(), topic, "1")
                    }
                    dialog.dismiss()
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }

            val calendarEdit = CalendarEdit(
                accountId = "1",
                presence = isPresence,
                // Usando a data selecionada pelo usuário:
                date = selectedDate.formatDateForJson(),
                dayOfWeek = selectedDate.formatDayOfWeek().toUpperCase(Locale.getDefault())
            )

            calendarEdits.add(calendarEdit)

            val presenceMessage = if (isPresence) "Presença marcada" else "Falta marcada"
            Toast.makeText(
                requireContext(),
                "$presenceMessage para ${selectedDate.getFormattedDate()}",
                Toast.LENGTH_SHORT
            ).show()

            sendAttendanceDataToAPI(calendarEdit)
        }

        buttonPresence.setOnClickListener(interactionListener)
        buttonAbsence.setOnClickListener(interactionListener)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
        }

        return rootView
    }

    private fun sendAttendanceDataToAPI(calendarEdit: CalendarEdit) {
        val apiUrl = "${UrlManager.baseUrl}/presence/update"
        Fuel.post(apiUrl)
            .jsonBody(calendarEdit.toJson())
            .response { result ->
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Dados enviados com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Failure -> {
                        val errorMessage = result.getException().localizedMessage
                        Toast.makeText(
                            requireContext(),
                            "Erro ao enviar dados: $errorMessage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun sendTopicEditToAPI(date: String, topic: String, accountId: String) {
        val topicEdit = TopicEdit(
            accountId = accountId,
            date = date,
            topic = topic
        )

        val topicApiUrl = "${UrlManager.baseUrl}/presence/class-content"

        Fuel.post(topicApiUrl)
            .jsonBody(formatTopicEditToJson(topicEdit))
            .response { result ->
                activity?.runOnUiThread {
                    when (result) {
                        is Result.Success -> {
                            Toast.makeText(
                                requireContext(),
                                "Tópico perdido registrado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Failure -> {
                            val errorMessage = result.getException().localizedMessage
                            Toast.makeText(
                                requireContext(),
                                "Erro ao registrar tópico perdido: $errorMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }


    private fun Calendar.getFormattedDate(): String {
        return sdf.format(time)
    }

    private fun Calendar.formatDateForJson(): String {
        return sdfJson.format(time)
    }

    private fun Calendar.formatDayOfWeek(): String {
        return sdfDayOfWeek.format(time)
    }

    private fun CalendarEdit.toJson(): String {
        return """
            {
                "accountId": "$accountId",
                "presence": $presence,
                "date": "$date",
                "dayOfWeek": "$dayOfWeek"
            }
        """.trimIndent()
    }

    private fun formatTopicEditToJson(topicEdit: TopicEdit): String {
        return """
            {
                "accountId": "${topicEdit.accountId}",
                "date": "${topicEdit.date}",
                "topic": "${topicEdit.topic}"
            }
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
