package com.github.ygyin.kotlinaudioplayer.ui.nowplaying

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.ygyin.kotlinaudioplayer.R
import com.github.ygyin.kotlinaudioplayer.R.color
import com.github.ygyin.kotlinaudioplayer.ui.nowplaying.NowPlayingViewModel.NowPlayingMetadata.Companion.timing
import com.github.ygyin.kotlinaudioplayer.ui.playlist.PlaylistViewModel
import com.github.ygyin.kotlinaudioplayer.utils.Injector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.now_playing_content.*
import kotlinx.android.synthetic.main.now_playing_fragment.*
import kotlinx.android.synthetic.main.now_playing_main_content.*

private const val NowPlaying_TAG = "NowPlayingFragment"

class NowPlayingFragment : Fragment() {


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val playlistViewModel: PlaylistViewModel by viewModels {
        Injector.providePlaylistViewModel(requireContext())
    }

    private val nowPlayingViewModel: NowPlayingViewModel by viewModels {
        Injector.provideNowPlayingViewModel(requireContext())
    }

    private var shuffleFlag = 0

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
            Observer { mediaItem -> uiUpdate(view, mediaItem) })

        nowPlayingViewModel.playbackProcess.observe(viewLifecycleOwner,
            Observer { progress: Int -> updateProgressBar(progress) })

        nowPlayingViewModel.playPauseButton.observe(viewLifecycleOwner,
            Observer {
                playPauseImage.setImageState(it, true)
                mainPlayPauseButton.setImageState(it, true)
            })

        nowPlayingViewModel.shuffleMode.observe(viewLifecycleOwner,
            Observer {
                when(it){
                    PlaybackStateCompat.SHUFFLE_MODE_NONE -> {
                       shuffleButton.setColorFilter(Color.BLACK)
                   }
                   PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
                       shuffleButton.setColorFilter(color.purple_500)
                   }
                }
            }
        )

        nowPlayingViewModel.repeatMode.observe(viewLifecycleOwner,
            Observer { when (it) {
                    PlaybackStateCompat.REPEAT_MODE_NONE -> {
                        repeatButton.setImageResource(R.drawable.ic_repeat)
                        repeatButton.setColorFilter(Color.BLACK)
                    }
                    PlaybackStateCompat.REPEAT_MODE_ONE -> {
                        repeatButton.setImageResource(R.drawable.ic_repeat_one)
                        repeatButton.setColorFilter(color.purple_500)
                    }
                    PlaybackStateCompat.REPEAT_MODE_ALL -> {
                        repeatButton.setImageResource(R.drawable.ic_repeat)
                        repeatButton.setColorFilter(color.purple_500)
                    }
                }
            }
        )

        nowPlayingViewModel.audioPosition.observe(viewLifecycleOwner,
            Observer { position -> nowDuration.text = timing(requireContext(), position) })

        playPauseImage.setOnClickListener {
            nowPlayingViewModel.audioMetadata.value?.let {
                playlistViewModel.playAudioById(it.id)
            }
        }

        mainPlayPauseButton.setOnClickListener {
            nowPlayingViewModel.audioMetadata.value?.let {
                playlistViewModel.playAudioById(it.id)
            }
        }

        mainPreviousButton.setOnClickListener {
            nowPlayingViewModel.skipPrevious()
        }

        mainNextButton.setOnClickListener {
            nowPlayingViewModel.skipNext()
        }

        repeatButton.setOnClickListener {
            nowPlayingViewModel.selectRepeatMode()
        }

        shuffleButton.setOnClickListener {
            nowPlayingViewModel.selectShuffleMode()

        }

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
                    android.R.color.white
                )
            )
            // Update title text color
            mainTitle.setTextColor(Color.WHITE)
            mainSubTitle.setTextColor(Color.WHITE)
        } else {
            // bottom cover
            Glide.with(view)
                .load(metadata.coverUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(bottomCover)
            // Main cover
            Glide.with(view)
                .load(metadata.coverUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mainCover)

            Glide.with(view)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(metadata.coverUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Palette.from(resource).generate { palette ->
                            if (palette == null) return@generate

                            mainTitle.setTextColor(
                                ContextCompat.getColor(requireContext(), android.R.color.black)
                            )
                            mainSubTitle.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.darker_gray
                                )
                            )
//                            setMainTitleColor(palette)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        bottomTitle.text = metadata.title
        bottomSubTitle.text = metadata.subtitle

        mainTitle.text = metadata.title
        mainSubTitle.text = metadata.subtitle

        totalDuration.text = metadata.playTime
    }

    private fun setMainTitleColor(palette: Palette) {
        val bodyColor: Int = palette.getDominantColor(
            ContextCompat.getColor(requireContext(), android.R.color.black)
        )

        val titleTextColor =
            palette.getLightVibrantColor(
                ContextCompat.getColor(requireContext(), android.R.color.white)
            )

        val bodyTextColor =
            palette.getLightMutedColor(
                ContextCompat.getColor(requireContext(), android.R.color.white)
            )

        titleBg.setBackgroundColor(bodyColor)

    }

    private fun setBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(nowPlayingButtonSheetFragment)
        bottomSheetBehavior.isHideable = false

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d(NowPlaying_TAG, "Slide Offset: $slideOffset")
                NowPlayingContent.alpha = 1 - slideOffset
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

    private var lastProgress = 0
    private fun updateProgressBar(progress: Int) {
        if (progress == lastProgress)
            return
        lastProgress = progress
        bottomSeekBar.setProgress(progress, true)
        mainSeekBar.setProgress(progress, true)
    }
}