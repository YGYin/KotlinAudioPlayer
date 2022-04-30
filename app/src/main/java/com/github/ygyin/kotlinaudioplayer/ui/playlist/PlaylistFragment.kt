package com.github.ygyin.kotlinaudioplayer.ui.playlist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ygyin.kotlinaudioplayer.R
import com.github.ygyin.kotlinaudioplayer.data.Music
import com.github.ygyin.kotlinaudioplayer.utils.Injector
import kotlinx.android.synthetic.main.fragment_playlist.*

private const val READ_EXTERNAL_STORAGE_REQUEST = 1

class PlaylistFragment : Fragment() {

    companion object{
        fun newInstance() = PlaylistFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val musicAdapter = PlaylistAdapter{
            clickMusic(it)
        }

        with(musicRecyclerView){
            layoutManager=LinearLayoutManager(activity)
            adapter= musicAdapter
        }
        playlistViewModel.audio.observe(this.viewLifecycleOwner, Observer<List<Music>>{
                audio -> musicAdapter.submitList(audio)
        })

        if (haveStoragePermission())
            loadList()
        else
            requestPermission()
    }

    private val playlistViewModel: PlaylistViewModel by viewModels() {
        Injector.providePlaylistViewModel(requireContext())
    }

    // 3 Steps for asking permissions
    // 1. Ask for the permission of READ_EXTERNAL_STORAGE
    private fun requestPermission(){
        if(!haveStoragePermission()){
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(
                permissions,
                READ_EXTERNAL_STORAGE_REQUEST
            )
        }
    }

    // 2. To check whether get the permission or not
    private fun haveStoragePermission()=
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )== PackageManager.PERMISSION_GRANTED

    /*
        3. When user accepts the permission (after clicking ACCEPT button),
        it should provide the playlist to User
    */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){
            // when requestCode == 1
            READ_EXTERNAL_STORAGE_REQUEST ->{
                if (grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    // TODO: Show the song list
                    loadList()
                }
            }
        }
    }

    // Display the music
    private fun loadList(){
        playlistViewModel.loadAudio()
    }

    private fun clickMusic(music: Music) {
        // TODO： Play music
    }

}