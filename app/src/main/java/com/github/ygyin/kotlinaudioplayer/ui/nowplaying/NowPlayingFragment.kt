package com.github.ygyin.kotlinaudioplayer.ui.nowplaying

import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.ygyin.kotlinaudioplayer.R
import com.github.ygyin.kotlinaudioplayer.utils.Injector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.now_playing_content.*
import kotlinx.android.synthetic.main.now_playing_fragment.*
import kotlinx.android.synthetic.main.now_playing_main_content.*

private const val NowPlaying_TAG = "NowPlayingFragment"

class NowPlayingFragment : Fragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val nowPlayingViewModel: NowPlayingViewModel by viewModels {
        Injector.provideNowPlayingViewModel(requireContext())
    }

//    private val playlistViewModel: PlaylistViewModel by viewModels {
//        Injector.providePlaylistViewModel(requireContext())
//    }

    companion object {
        fun newInstance() = NowPlayingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.now_playing_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomSheetBehavior()
        // Disable seek in small seekbar
        bottomSeekBar.setOnTouchListener { _, _ -> true }

        // To observe the livedata comes from NowPlayingViewModel
        nowPlayingViewModel.audioMetadata.observe(viewLifecycleOwner,
            Observer{ mediaItem -> uiUpdate(view, mediaItem) })

        nowPlayingViewModel.playbackProcess.observe(viewLifecycleOwner,
            Observer { progress: Int ->
                updateProgressBar(progress)
            })

//        nowPlayingViewModel.audioPosition.observe(viewLifecycleOwner,
//            Observer { position -> nowDuration.text = timing(requireContext(), position) })
    }

    private fun uiUpdate(view: View, metadata: NowPlayingViewModel.NowPlayingMetadata) {
        if (metadata.coverUri == Uri.EMPTY) {
            // Update the bottom cover and main cover
            bottomCover.setImageResource(R.drawable.ic_default_cover)
            bottomCover.setBackgroundResource(R.drawable.ic_default_cover_bg)
            mainCover.setImageResource(R.drawable.ic_default_cover)
            mainCover.setBackgroundResource(R.drawable.ic_default_cover_bg)
            // Update background color
            titleBg.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.google.android.material.R.color.design_default_color_primary
                )
            )
            // Update title text color
            mainTitle.setTextColor(Color.WHITE)
            mainSubTitle.setTextColor(Color.WHITE)
        } else {
            Glide.with(view)
                .load(metadata.coverUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(bottomCover)
            bottomTitle.text = metadata.title
            bottomSubTitle.text = metadata.subtitle
        }
    }

    private fun setBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(nowPlayingButtonSheetFragment)
        bottomSheetBehavior.isHideable = false

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d(NowPlaying_TAG, "Slide Offset: $slideOffset")
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> Log.d(NowPlaying_TAG, "STATE_COLLAPSED")
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Log.d(NowPlaying_TAG, "STATE_DRAGGING")
                        NowPlayingContent.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d(NowPlaying_TAG, "STATE_EXPANDED")
                        NowPlayingContent.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> Log.d(NowPlaying_TAG, "STATE_HIDDEN")
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Log.d(
                        NowPlaying_TAG,
                        "STATE_HALF_EXPANDED"
                    )
                    BottomSheetBehavior.STATE_SETTLING -> Log.d(NowPlaying_TAG, "STATE_SETTLING")
                }
            }
        })
    }

    private fun updateProgressBar(progress: Int) {
        bottomSeekBar.setProgress(progress, true)
    }
}