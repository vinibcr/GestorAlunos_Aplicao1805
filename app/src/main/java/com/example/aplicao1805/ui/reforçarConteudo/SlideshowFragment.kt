package com.example.aplicao1805.ui.reforçarConteudo

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.aplicao1805.R
import com.example.aplicao1805.databinding.FragmentSlideshowBinding
import com.example.aplicao1805.ui.home.UrlManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.typesafe.config.ConfigFactory
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    private lateinit var firstLinearLayout: LinearLayout
    private lateinit var secondLinearLayout: LinearLayout
    private lateinit var dayOfWeekSpinner: Spinner
    private lateinit var segundaFeiraButton: Button
    private lateinit var tercaFeiraButton: Button
    private lateinit var quartaFeiraButton: Button
    private lateinit var quintaFeiraButton: Button
    private lateinit var sextaFeiraButton: Button
    private lateinit var outrosButton: Button
    private lateinit var textSlideshow: TextView
    private lateinit var topicoEditText: EditText

    private lateinit var selectedTopic: String
    private lateinit var quizCardView1: CardView
    private lateinit var quizCardView2: CardView
    private lateinit var quizCardView3: CardView
    private lateinit var questionTextView1: TextView
    private lateinit var questionTextView2: TextView
    private lateinit var questionTextView3: TextView
    private var correctAnswers1: String = ""
    private var correctAnswers2: String = ""
    private var correctAnswers3: String = ""

    // Dentro da classe SlideshowFragment
    private lateinit var A1Button: Button
    private lateinit var B1Button: Button
    private lateinit var C1Button: Button
    private lateinit var D1Button: Button
    private lateinit var submitButton1: Button

    private lateinit var A2Button: Button
    private lateinit var B2Button: Button
    private lateinit var C2Button: Button
    private lateinit var D2Button: Button
    private lateinit var submitButton2: Button

    private lateinit var A3Button: Button
    private lateinit var B3Button: Button
    private lateinit var C3Button: Button
    private lateinit var D3Button: Button
    private lateinit var submitButton3: Button



    // Variável para armazenar a lista de tópicos obtidos do JSON
    private val topicsList = mutableListOf<String>()

    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicialize os objetos com base nos IDs do layout XML
        firstLinearLayout = binding.firstLinearLayout

        dayOfWeekSpinner = binding.dayOfWeekSpinner
        segundaFeiraButton = binding.segundaFeiraButton
        tercaFeiraButton = binding.tercaFeiraButton
        quartaFeiraButton = binding.quartaFeiraButton
        quintaFeiraButton = binding.quintaFeiraButton
        sextaFeiraButton = binding.sextaFeiraButton
        textSlideshow = binding.textSlideshow
        topicoEditText = binding.topicoEditText
        outrosButton = binding.outrosButton
        quizCardView1 = binding.quizCardView1
        quizCardView2 = binding.quizCardView2
        quizCardView3 = binding.quizCardView3
        questionTextView1 = binding.questionTextView1
        questionTextView2 = binding.questionTextView2
        questionTextView3 = binding.questionTextView3

        // Dentro do método onCreateView
        A1Button = binding.A1Button
        B1Button = binding.B1Button
        C1Button = binding.C1Button
        D1Button = binding.D1Button
        submitButton1 = binding.submitButton1

        A2Button = binding.A2Button
        B2Button = binding.B2Button
        C2Button = binding.C2Button
        D2Button = binding.D2Button
        submitButton2 = binding.submitButton2

        A3Button = binding.A3Button
        B3Button = binding.B3Button
        C3Button = binding.C3Button
        D3Button = binding.D3Button
        submitButton3 = binding.submitButton3


        val buttonGoToQuiz: Button = root.findViewById(R.id.ir_para_quiz_button)
        buttonGoToQuiz.visibility = View.INVISIBLE

        // Define o adaptador inicial para o Spinner (vazio)
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            topicsList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dayOfWeekSpinner.adapter = adapter

        // Configura os ouvintes de clique para os botões de segunda-feira a sexta-feira
        val buttons = listOf(
            segundaFeiraButton,
            tercaFeiraButton,
            quartaFeiraButton,
            quintaFeiraButton,
            sextaFeiraButton,
            outrosButton
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                val selectedDayOfWeek = button.text.toString().trim()

                buttonGoToQuiz.visibility = View.VISIBLE
                dayOfWeekSpinner.visibility = if (button != outrosButton) View.VISIBLE else View.GONE
                topicoEditText.visibility = if (button == outrosButton) View.VISIBLE else View.GONE

                if (button != outrosButton) {
                    fetchTopicData(selectedDayOfWeek)
                }
            }
        }




        A1Button.setOnClickListener(answerButtonClickListener)
        B1Button.setOnClickListener(answerButtonClickListener)
        C1Button.setOnClickListener(answerButtonClickListener)
        D1Button.setOnClickListener(answerButtonClickListener)

        A2Button.setOnClickListener(answerButtonClickListener)
        B2Button.setOnClickListener(answerButtonClickListener)
        C2Button.setOnClickListener(answerButtonClickListener)
        D2Button.setOnClickListener(answerButtonClickListener)

        A3Button.setOnClickListener(answerButtonClickListener)
        B3Button.setOnClickListener(answerButtonClickListener)
        C3Button.setOnClickListener(answerButtonClickListener)
        D3Button.setOnClickListener(answerButtonClickListener)

        buttonGoToQuiz.setOnClickListener {
            if (dayOfWeekSpinner.visibility == View.VISIBLE) {
                val selectedTopic = dayOfWeekSpinner.selectedItem?.toString()?.trim()

                if (selectedTopic.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Selecione um tópico válido.", Toast.LENGTH_SHORT).show()
                } else {
                    sendTopicToAPI(selectedTopic)
                }
            } else if (topicoEditText.visibility == View.VISIBLE) {
                val topicoEditTextValue = topicoEditText.text.toString().trim()

                if (topicoEditTextValue.isNotBlank()) {
                    sendTopicToAPI(topicoEditTextValue)
                } else {
                    Toast.makeText(requireContext(), "Insira um tópico válido.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Selecione um tópico válido.", Toast.LENGTH_SHORT).show()
            }
        }



        return root
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTopicData(dayOfWeek: String) {

      //  val properties = Properties()
     //  val base = properties.getProperty("ENDPOINT_URL")
     //   val config = ConfigFactory.load()



      //  val base: String? = System.getProperty("URL")
        val endpointUrl = "${UrlManager.baseUrl}/presence/topic/1/$dayOfWeek"

        Fuel.get(endpointUrl)
            .responseString { result ->
                when (result) {
                    is Result.Success -> {
                        val responseString = result.get()

                        try {
                            val jsonObject = JSONObject(responseString)
                            val topicsArray = jsonObject.getJSONArray("topics")
                            topicsList.clear()
                            for (i in 0 until topicsArray.length()) {
                                val topic = topicsArray.getString(i)
                                topicsList.add(topic)
                            }

                            activity?.runOnUiThread {
                                (dayOfWeekSpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    is Result.Failure -> {
                        val errorMessage = result.getException().localizedMessage
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Erro na chamada GET: $errorMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    private fun sendTopicToAPI(selectedTopic: String) {
        val apiUrl = "${UrlManager.baseUrl}/openai/quiz"
        val topicJson = JSONObject()
        topicJson.put("topic", selectedTopic)

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Carregando...") // Defina a mensagem do progress dialog
        progressDialog.show()

        Fuel.post(apiUrl)
            .jsonBody(topicJson.toString())
            .timeoutRead(60000)
            .timeout(60000)
            .responseString { result ->
                progressDialog.dismiss() // Esconde o progress dialog após a conclusão, seja bem-sucedida ou não

                when (result) {
                    is Result.Success -> {
                        val responseString = result.get()
                        Log.d("API Response (Success)", responseString)

                        try {
                            if (isJSONValid(responseString)) {


                                val jsonObject = JSONObject(responseString)
                                val questionsArray = jsonObject.getJSONArray("questions")
                                val answersArray = jsonObject.optJSONArray("answers")
                                if (jsonObject.has("questions") && jsonObject.has("answers")
                                    && questionsArray.length() == 3) {


                                    for (i in 0 until questionsArray.length()) {
                                        val question = questionsArray.getString(i)


                                        // Obtenha as respostas corretas da resposta JSON
                                        if (answersArray != null && i < answersArray.length()) {
                                            val answers = answersArray.getString(i)
                                            val splitAnswers = answers.split(',')
                                            if (splitAnswers.size == 3) {
                                                correctAnswers1 = splitAnswers[0]
                                                correctAnswers2 = splitAnswers[1]
                                                correctAnswers3 = splitAnswers[2]

                                            }
                                            else{
                                                activity?.runOnUiThread {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Tente novamente, erro ao montar o Quiz 1",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                        else{
                                            activity?.runOnUiThread {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Tente novamente, erro ao montar o Quiz 2",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        // Atualize o TextView correspondente com a pergunta
                                        when (i) {

                                            0 -> {
                                                activity?.runOnUiThread {
                                                    questionTextView1.text = question
                                                }
                                            }
                                            1 -> {
                                                activity?.runOnUiThread {
                                                    questionTextView2.text = question
                                                }
                                            }
                                            2 -> {
                                                activity?.runOnUiThread {
                                                    questionTextView3.text = question
                                                }
                                            }
                                        }

                                    }
                                    activity?.runOnUiThread {
                                        if (!questionTextView1.text.isNullOrEmpty() &&
                                            !questionTextView2.text.isNullOrEmpty() &&
                                            !questionTextView3.text.isNullOrEmpty() &&
                                            correctAnswers1.length == 1 &&
                                            correctAnswers2.length == 1 &&
                                            correctAnswers3.length == 1 &&
                                            correctAnswers1.isNotEmpty() &&
                                            correctAnswers2.isNotEmpty() &&
                                            correctAnswers3.isNotEmpty()
                                        ) {
                                            firstLinearLayout.visibility = View.GONE
                                            quizCardView1.visibility = View.VISIBLE
                                            quizCardView2.visibility = View.VISIBLE
                                            quizCardView3.visibility = View.VISIBLE

                                            Log.d("Correct Answer 1", correctAnswers1)
                                            Log.d("Correct Answer 2", correctAnswers2)
                                            Log.d("Correct Answer 3", correctAnswers3)

                                        }
                                        else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Tente novamente, erro ao montar o Quiz 3",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            Log.e("API Response", "Resposta JSON não contém o campo 'questions'")
                                        }


                                    }
                                } else {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            requireContext(),
                                            "Tente novamente, erro ao montar o Quiz 4",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }


                            } else {
                                activity?.runOnUiThread {
                                    Toast.makeText(
                                        requireContext(),
                                        "Resposta não é um Json valido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()

                        }


                    }

                    is Result.Failure -> {
                        val errorMessage = result.getException().localizedMessage
                        if (errorMessage != null) {
                            Log.e("API Response (Failure)", errorMessage)
                        }
                        Toast.makeText(
                            requireContext(),
                            "Erro no servidor: $errorMessage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }



    // Adicione esta função após a classe SlideshowFragment
    private fun checkAnswer(correctAnswer: String, selectedAnswer: String, questionNumber: Int) {
        val submitButton: Button = when (questionNumber) {
            1 -> submitButton1
            2 -> submitButton2
            3 -> submitButton3
            else -> submitButton1
        }

        if (correctAnswer == selectedAnswer) {
            submitButton.visibility = View.VISIBLE
            submitButton.setBackgroundColor(Color.GREEN)
            submitButton.setTextColor(Color.BLACK)
            submitButton.text = "Resposta correta"
            Toast.makeText(requireContext(), "Resposta correta!", Toast.LENGTH_SHORT).show()
        } else {
            submitButton.visibility = View.VISIBLE
            submitButton.setTextColor(Color.BLACK)
            submitButton.setBackgroundColor(Color.RED)
            submitButton.text = "Resposta incorreta"
            Toast.makeText(requireContext(), "Resposta incorreta!", Toast.LENGTH_SHORT).show()
        }
    }


    private val answerButtonClickListener = View.OnClickListener { view ->
        val selectedButton = view as Button
        val questionNumber = when (selectedButton) {
            A1Button, B1Button, C1Button, D1Button -> 1
            A2Button, B2Button, C2Button, D2Button -> 2
            A3Button, B3Button, C3Button, D3Button -> 3
            else -> 0
        }

        val correctAnswer = when (questionNumber) {
            1 -> correctAnswers1
            2 -> correctAnswers2
            3 -> correctAnswers3
            else -> ""
        }

        val selectedAnswer = selectedButton.text.toString()

        checkAnswer(correctAnswer, selectedAnswer, questionNumber)
    }




    private fun isJSONValid(jsonString: String): Boolean {
        return try {
            JSONObject(jsonString)
            true
        } catch (e: JSONException) {
            false
        }
    }
}
