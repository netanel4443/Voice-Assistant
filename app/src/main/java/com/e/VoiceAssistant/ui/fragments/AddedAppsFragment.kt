package com.e.VoiceAssistant.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.AddedAppsRecyclerAdapter
import com.e.VoiceAssistant.userscollecteddata.AppsDetailsSingleton
import com.e.VoiceAssistant.viewmodels.AddCustomAppNameViewModel
import com.e.VoiceAssistant.viewmodels.states.AddCustomAppNameStates
import kotlinx.android.synthetic.main.fragment_added_apps.*
import javax.inject.Inject

class AddedAppsFragment : BaseFragment() {
    private val TAG="BaseFragment"
    private lateinit var adapter: AddedAppsRecyclerAdapter
    var addedApps=HashMap<String,AppsDetails>()
    private val viewModel:AddCustomAppNameViewModel by lazy(this::getViewModel)

    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_added_apps, container, false)

        addedApps=appsDetailsSingleton.storedAppsDetailsFromDB
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        viewModel.getState().observe(viewLifecycleOwner, Observer {state->
            when(state){
                 is AddCustomAppNameStates.RemoveItemFromAppList->removeItemfromServiceAppList()
            }
        })
    }


    private fun initRecyclerView() {

        val recyclerView=recyclerFragAddedApps
        adapter= AddedAppsRecyclerAdapter(addedApps){ name->
            viewModel.deleteAppFromList(name)
        }
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(requireActivity(),RecyclerView.VERTICAL,false)
        recyclerView.setHasFixedSize(true)
    }

    private fun removeItemfromServiceAppList(){
        adapter.notifyDataSetChanged()
    }

}
