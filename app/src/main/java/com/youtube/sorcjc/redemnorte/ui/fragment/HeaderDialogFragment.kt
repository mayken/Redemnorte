package com.youtube.sorcjc.redemnorte.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.*
import com.youtube.sorcjc.redemnorte.Global
import com.youtube.sorcjc.redemnorte.R
import com.youtube.sorcjc.redemnorte.io.MyApiAdapter
import com.youtube.sorcjc.redemnorte.io.response.HojaResponse
import com.youtube.sorcjc.redemnorte.io.response.ResponsableResponse
import com.youtube.sorcjc.redemnorte.io.response.SimpleResponse
import com.youtube.sorcjc.redemnorte.model.Responsable
import com.youtube.sorcjc.redemnorte.model.Sheet
import com.youtube.sorcjc.redemnorte.ui.activity.PanelActivity
import kotlinx.android.synthetic.main.dialog_new_header.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class HeaderDialogFragment : DialogFragment() {
    private var hoja_id: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hoja_id = arguments!!.getString("hoja_id")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_new_header, container, false)

        val title: String
        if (hoja_id!!.isEmpty()) title = "Registrar nueva hoja" else {
            title = "Editar hoja"
            fetchHeaderDataFromServer()
            etId.setText(hoja_id)
            etId.isEnabled = false
        }
        
        toolbar?.title = title
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        }
        setHasOptionsMenu(true)

        obtenerDatosResponsables()

        // spinnerResponsible = view.findViewById<View>(R.id.spinnerResponsible) as AutoCompleteTextView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hoja_id!!.isEmpty()) { // set for new headers (for edit mode will be set later)
            setCheckPendienteOnChangeListener()
        }
    }

    private fun setCheckPendienteOnChangeListener() {
        checkPendiente.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tilObservation.visibility = View.VISIBLE
                Global.showInformationDialog(context, "Observación", "¿Por qué motivo la hoja se ha marcado como pendiente?")
            } else {
                tilObservation.visibility = View.GONE
                etObservation.setText("")
            }
        }
    }

    private fun poblarSpinnerResponsables(responsables: ArrayList<Responsable>) {
        val list: MutableList<String?> = ArrayList()
        for ((nombre) in responsables) {
            list.add(nombre)
        }
        val spinnerArrayAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, list)
        // spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResponsible!!.setAdapter(spinnerArrayAdapter)
    }

    private fun obtenerDatosResponsables() {
        val call = MyApiAdapter.getApiService().responsables
        call.enqueue(ResponsablesCallback())
    }

    internal inner class ResponsablesCallback : Callback<ResponsableResponse?> {
        override fun onResponse(call: Call<ResponsableResponse?>, response: Response<ResponsableResponse?>) {
            if (response.isSuccessful) {
                val responsableResponse = response.body()
                if (!responsableResponse!!.isError) {
                    poblarSpinnerResponsables(responsableResponse.responsables)
                }
            } else {
                Toast.makeText(context, "Error en el formato de respuesta", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<ResponsableResponse?>, t: Throwable) {
            Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.save) {
            validateForm()
            return true
        } else if (id == android.R.id.home) {
            // handle close button click here
            dismiss()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateForm() {
        if (!validateEditText(etId, tilId, R.string.error_hoja_id)) {
            return
        }
        if (!validateEditText(etLocal, tilLocal, R.string.error_local)) {
            return
        }
        if (!validateEditText(etUbicacion, tilUbicacion, R.string.error_ubicacion)) {
            return
        }
        if (!validateEditText(etCargo, tilCargo, R.string.error_cargo)) {
            return
        }
        if (!validateEditText(etOficina, tilOficina, R.string.error_oficina)) {
            return
        }
        if (!validateEditText(etAmbiente, tilAmbiente, R.string.error_ambiente)) {
            return
        }
        if (!validateEditText(etArea, tilArea, R.string.error_area)) {
            return
        }

        val id = etId!!.text.toString().trim { it <= ' ' }
        val local = etLocal!!.text.toString().trim { it <= ' ' }
        val ubicacion = etUbicacion!!.text.toString().trim { it <= ' ' }
        val responsable = spinnerResponsible!!.text.toString().trim { it <= ' ' }
        val cargo = etCargo!!.text.toString().trim { it <= ' ' }
        val oficina = etOficina!!.text.toString().trim { it <= ' ' }
        val ambiente = etAmbiente!!.text.toString().trim { it <= ' ' }
        val area = etArea!!.text.toString().trim { it <= ' ' }
        val activo = if (checkPendiente.isChecked) "0" else "1"
        val observacion = etObservation!!.text.toString().trim { it <= ' ' }


        // If we have received an ID, we have to edit the data, else, we have to create a new record
        if (hoja_id!!.isEmpty()) {
            val inventariador = Global.getFromSharedPreferences(activity, "username")
            val call = MyApiAdapter.getApiService().storeSheet(
                    id, local, ubicacion, responsable, cargo, oficina,
                    ambiente, area, activo, observacion, inventariador
            )
            call.enqueue(RegistrarHojaCallback())
        } else {
            val call = MyApiAdapter.getApiService().updateSheet(
                    id, local, ubicacion, responsable, cargo, oficina,
                    ambiente, area, activo, observacion
            )
            call.enqueue(EditarHojaCallback())
        }
    }

    internal inner class RegistrarHojaCallback : Callback<SimpleResponse?> {
        override fun onResponse(call: Call<SimpleResponse?>, response: Response<SimpleResponse?>) {
            if (response.isSuccessful) {
                val simpleResponse = response.body()
                if (simpleResponse!!.isError) {
                    // Log.d("HeaderDialog", "messageError => " + simpleResponse.getMessage());
                    Toast.makeText(context, simpleResponse.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Se ha registrado una nueva hoja", Toast.LENGTH_SHORT).show()
                    // Re-load the sheets
                    (activity as PanelActivity?)?.loadInventorySheets()
                    dismiss()
                }
            } else {
                Toast.makeText(context, "Error en el formato de respuesta", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<SimpleResponse?>, t: Throwable) {
            Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    internal inner class EditarHojaCallback : Callback<SimpleResponse?> {
        override fun onResponse(call: Call<SimpleResponse?>, response: Response<SimpleResponse?>) {
            if (response.isSuccessful) {
                val simpleResponse = response.body()
                if (simpleResponse!!.isError) {
                    Toast.makeText(context, simpleResponse.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Se ha editado correctamente la hoja", Toast.LENGTH_SHORT).show()
                    // Re-load the sheets
                    (activity as PanelActivity?)?.loadInventorySheets()
                    dismiss()
                }
            } else {
                Toast.makeText(context, "Error en el formato de respuesta", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<SimpleResponse?>, t: Throwable) {
            Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateEditText(editText: EditText?, textInputLayout: TextInputLayout?, errorString: Int): Boolean {
        if (editText!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            textInputLayout!!.error = getString(errorString)
            return false
        } else {
            textInputLayout!!.isErrorEnabled = false
        }
        return true
    }

    private fun fetchHeaderDataFromServer() {
        val call = MyApiAdapter.getApiService().getSheet(hoja_id)
        call.enqueue(ShowHeaderDataCallback())
    }

    internal inner class ShowHeaderDataCallback : Callback<HojaResponse?> {
        override fun onResponse(call: Call<HojaResponse?>, response: Response<HojaResponse?>) {
            if (response.isSuccessful) {
                val hojaResponse = response.body()
                if (hojaResponse!!.isError) {
                    Toast.makeText(context, hojaResponse.message, Toast.LENGTH_SHORT).show()
                } else {
                    showHeaderDataInFields(hojaResponse.sheet)
                }
            } else {
                Toast.makeText(context, "Error en el formato de respuesta", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<HojaResponse?>, t: Throwable) {
            Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        private fun showHeaderDataInFields(sheet: Sheet) {
            etLocal.setText(sheet.local)
            etUbicacion.setText(sheet.ubicacion)
            etCargo.setText(sheet.cargo)
            etOficina.setText(sheet.oficina)
            etAmbiente.setText(sheet.ambiente)
            etArea.setText(sheet.area)
            spinnerResponsible.setText(sheet.responsable)

            if (sheet.activo == "0") { // active==0 => pendiente
                checkPendiente.isChecked = true
                // pendiente => show observation field
                tilObservation!!.visibility = View.VISIBLE
                etObservation!!.setText(sheet.observacion)
            }
            setCheckPendienteOnChangeListener()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(hoja_id: String?): HeaderDialogFragment {
            val f = HeaderDialogFragment()
            val args = Bundle()
            args.putString("hoja_id", hoja_id)
            f.arguments = args
            return f
        }
    }
}