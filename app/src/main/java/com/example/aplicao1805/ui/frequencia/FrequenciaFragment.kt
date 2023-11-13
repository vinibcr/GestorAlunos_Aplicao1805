package com.example.aplicao1805.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aplicao1805.R
import com.example.aplicao1805.databinding.FragmentFrequenciaBinding
import com.example.aplicao1805.ui.frequencia.FrequenciaViewModel
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import org.json.JSONObject

class FrequenciaFragment : Fragment() {
    private lateinit var exibButton: Button


    private var _binding: FragmentFrequenciaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        ViewModelProvider(this)[FrequenciaViewModel::class.java]

        _binding = FragmentFrequenciaBinding.inflate(inflater, container, false)
        val rootView: View = binding.root
        exibButton = rootView.findViewById(R.id.exib_button)

        val segundaFeiraButton: Button = rootView.findViewById(R.id.segunda_feira_button)
        val tercaFeiraButton: Button = rootView.findViewById(R.id.terca_feira_button)
        val quartaFeiraButton: Button = rootView.findViewById(R.id.quarta_feira_button)
        val quintaFeiraButton: Button = rootView.findViewById(R.id.quinta_feira_button)
        val sextaFeiraButton: Button = rootView.findViewById(R.id.sexta_feira_button)


        // Configuração para cada botão
        segundaFeiraButton.setOnClickListener {
            fetchFrequencyData("MONDAY")
        }
        tercaFeiraButton.setOnClickListener {
            fetchFrequencyData("TUESDAY")
        }
        quartaFeiraButton.setOnClickListener {
            fetchFrequencyData("WEDNESDAY")
        }
        quintaFeiraButton.setOnClickListener {
            fetchFrequencyData("THURSDAY")
        }
        sextaFeiraButton.setOnClickListener {
            fetchFrequencyData("FRIDAY")
        }
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchFrequencyData(dayOfWeek: String) {
        val endpointUrl = "${UrlManager.baseUrl}/frequency/1/$dayOfWeek"
        Fuel.get(endpointUrl)
            .responseString { result ->
                when (result) {
                    is Result.Success -> {
                        val responseString = result.get()

                        // Crie um objeto para armazenar a resposta JSON original
                        val originalResponse = OriginalResponse(responseString)

                        // Separe a parte da "frequencia" e "numbercurrentclasses"
                        val jsonObject = JSONObject(originalResponse.responseString)
                        val frequencia = String.format("%.2f", jsonObject.getString("frequency").toDouble())
                        val numbercurrentclasses = jsonObject.getString("numberCurrentClasses")

                        // Exiba a mensagem na tela
                        val mensagem = "Frequência de $frequencia%\nem $numbercurrentclasses dias de aulas"
                        activity?.runOnUiThread {
                            activity?.runOnUiThread {
                                exibButton.visibility = View.VISIBLE
                                exibButton.text = mensagem
                            }
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

    // armazena a resposta JSON original
    data class OriginalResponse(val responseString: String)
}
