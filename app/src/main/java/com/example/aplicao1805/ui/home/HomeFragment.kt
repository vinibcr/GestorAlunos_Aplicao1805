package com.example.aplicao1805.ui.home
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aplicao1805.databinding.FragmentHomeBinding
import com.example.aplicao1805.ui.gallery.TopicEdit

import com.github.kittinunf.fuel.Fuel
import org.json.JSONObject



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val saveButton1 = binding.SaveButton1
        val saveButton2 = binding.SaveButton2
        val saveButton3 = binding.SaveButton3
        val saveButton4 = binding.SaveButton4
        val saveButton5 = binding.SaveButton5

        saveButton1.setOnClickListener {
            sendInformationToURL("MONDAY",
                binding.MateriaEditText1,
                binding.ProfEditText1,
                binding.EmailEditText1,
                binding.GradesEditText1,
                binding.NotesEditText1    )
        }

        saveButton2.setOnClickListener {
            sendInformationToURL("TUESDAY",
                binding.MateriaEditText2,
                binding.ProfEditText2,
                binding.EmailEditText2,
                binding.GradesEditText2,
                binding.NotesEditText2    )
        }

        saveButton3.setOnClickListener {
            sendInformationToURL("WEDNESDAY",
                binding.MateriaEditText3,
                binding.ProfEditText3,
                binding.EmailEditText3,
                binding.GradesEditText3,
                binding.NotesEditText3    )
        }

        saveButton4.setOnClickListener {
            sendInformationToURL("THURSDAY",
                binding.MateriaEditText4,
                binding.ProfEditText4,
                binding.EmailEditText4,
                binding.GradesEditText4,
                binding.NotesEditText4    )
        }

        saveButton5.setOnClickListener {
            sendInformationToURL("FRIDAY",
                binding.MateriaEditText5,
                binding.ProfEditText5,
                binding.EmailEditText5,
                binding.GradesEditText5,
                binding.NotesEditText5    )
        }


        // Call fetchTopicData when the fragment is created
        fetchTopicData("MONDAY",
            binding.MateriaEditText1,
            binding.ProfEditText1,
            binding.EmailEditText1,
            binding.GradesEditText1,
            binding.NotesEditText1)
        fetchTopicData("TUESDAY",
            binding.MateriaEditText2,
            binding.ProfEditText2,
            binding.EmailEditText2,
            binding.GradesEditText2,
            binding.NotesEditText2)
        fetchTopicData("WEDNESDAY",
            binding.MateriaEditText3,
            binding.ProfEditText3,
            binding.EmailEditText3,
            binding.GradesEditText3,
            binding.NotesEditText3)
        fetchTopicData("THURSDAY",
            binding.MateriaEditText4,
            binding.ProfEditText4,
            binding.EmailEditText4,
            binding.GradesEditText4,
            binding.NotesEditText4)
        fetchTopicData("FRIDAY",
            binding.MateriaEditText5,
            binding.ProfEditText5,
            binding.EmailEditText5,
            binding.GradesEditText5,
            binding.NotesEditText5)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTopicData(dayOfWeek: String,
                               MateriaeditText: EditText,
                               ProfeditText: EditText,
                               EmaileditText: EditText,
                               GradeseditText: EditText,
                               NoteseditText: EditText) {
        val endpointUrl = "${UrlManager.baseUrl}/frequency/1/$dayOfWeek"

        Fuel.get(endpointUrl).responseString { _, response, result ->
            result.fold(
                success = { data ->
                    val jsonObject = JSONObject(data)
                    val subjectName = jsonObject.optString("subjectName", "")
                    val profName = jsonObject.optString("teacherName", "")
                    val emailName = jsonObject.optString("email", "")
                    val gradesName = jsonObject.optString("grades", "")
                    val notesName = jsonObject.optString("notes", "")

                    activity?.runOnUiThread {
                        MateriaeditText.setText(if (subjectName.isEmpty()) "" else subjectName)
                        ProfeditText.setText(if (profName.isEmpty()) "" else profName)
                        EmaileditText.setText(if (emailName.isEmpty()) "" else emailName)
                        GradeseditText.setText(if (gradesName.isEmpty()) "" else gradesName)
                        NoteseditText.setText(if (notesName.isEmpty()) "" else notesName)
                    }
                },
                failure = { error ->
                    error.printStackTrace()

                    val errorMessage = error.localizedMessage
                    if (response.statusCode == 404) {
                        // Lógica para lidar especificamente com o erro 404
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Fields for the day of the week $dayOfWeek are not filled.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Lógica para lidar com outros erros
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Erro na chamada GET: $errorMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }

    private fun sendInformationToURL(dayOfWeek: String,
                                     MateriaeditText: EditText,
                                     ProfeditText: EditText,
                                     EmaileditText: EditText,
                                     GradeseditText: EditText,
                                     NoteseditText: EditText)  {

        val Materia = MateriaeditText.text
        val Professor = ProfeditText.text
        val Email = EmaileditText.text
        val Media = GradeseditText.text
        val Anotacao = NoteseditText.text

        val endpointUrl = "${UrlManager.baseUrl}/frequency/update" // Substitua com o endpoint apropriado

        val json = """
        {
            "accountId": "1",
            "subjectName": "$Materia",
            "teacherName": "$Professor",
            "email": "$Email",
            "grades": "$Media",
            "notes": "$Anotacao",
            "dayOfWeek": "$dayOfWeek"
        }
    """.trimIndent()

        Fuel.post(endpointUrl)
            .header("Content-Type" to "application/json")
            .body(json)
            .response { result ->
                when (result) {
                    is com.github.kittinunf.result.Result.Failure -> {
                        val ex = result.getException()
                        ex.printStackTrace()
                        Toast.makeText(
                            requireContext(),
                            "Erro no servidor para salvar matéria: $Materia",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Handle failure here
                    }
                    is com.github.kittinunf.result.Result.Success -> {
                        // Handle success here
                        Toast.makeText(
                            requireContext(),
                            "Dados salvas para matéria: $Materia",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }




}

object UrlManager {
    var baseUrl = "*************************"
}
