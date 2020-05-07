package com.e.VoiceAssistant.ui.fragments


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.ui.recyclerviewsadapters.AddedAppsRecyclerAdapter
import com.e.VoiceAssistant.viewmodels.SpeechRecognitionViewModel
import com.e.VoiceAssistant.viewmodels.states.SettingsViewModelStates
import kotlinx.android.synthetic.main.fragment_added_apps.*

class AddedAppsFragment : BaseFragment() {
    private val TAG="BaseFragment"
    private lateinit var adapter: AddedAppsRecyclerAdapter
    private var addedAppsHmap= LinkedHashMap<String,Drawable?>()
    private val viewModel:SpeechRecognitionViewModel by lazy(this::getViewModel)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_added_apps, container, false)

        viewModel.getListOrCachedApplist()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCachedAppList().observe(viewLifecycleOwner, Observer {
            initRecyclerView(it)
        })

        viewModel.getState().observe(viewLifecycleOwner, Observer {state->
            when(state){
                is SettingsViewModelStates.PassAppsToFragment->{
                    initRecyclerView(state.list)
                }
                is SettingsViewModelStates.AddItemToAppList-> addItemToServiceAppList(state.name,state.icon)
                is SettingsViewModelStates.RemoveItemFromAppList->removeItemfromServiceAppList(state.name)
            }
        })
    }


    private fun initRecyclerView(list:LinkedHashMap<String,Drawable?>) {
        addedAppsHmap=list

        val recyclerView=recyclerFragAddedApps
     //   println("list size ${list.size}")
        adapter= AddedAppsRecyclerAdapter(addedAppsHmap){ name->
            viewModel.deleteAppFromList(name)
        }
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(requireActivity(),RecyclerView.VERTICAL,false)
        recyclerView.setHasFixedSize(true)
    }
    private fun addItemToServiceAppList(name: String,icon:Drawable?){
        addedAppsHmap[name]=icon
    //    printMessage(TAG,name)
        adapter.notifyDataSetChanged()
    }
    private fun removeItemfromServiceAppList(name:String){
        addedAppsHmap.remove(name)
        adapter.notifyDataSetChanged()
    }


}
