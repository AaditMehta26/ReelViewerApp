@file:Suppress("DEPRECATION")

package com.example.reelviewerapp

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import java.io.File

class ReelFragment : Fragment() {

    private lateinit var playerView: PlayerView
    private lateinit var thumbsUpButton: ImageButton
    private lateinit var thumbsDownButton: ImageButton
    private var exoPlayer: ExoPlayer? = null
    private var videoResId: Int = 0
    private var currentFolder: String = ""
    private var videoFile: File? = null

    companion object {
        private const val ARG_VIDEO_RES_ID = "video_res_id"

        fun newInstance(videoResId: Int): ReelFragment {
            val fragment = ReelFragment()
            val args = Bundle()
            args.putInt(ARG_VIDEO_RES_ID, videoResId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allVideos = listOf(R.raw.video1, R.raw.video2, R.raw.video3, R.raw.video4, R.raw.video5, R.raw.video6, R.raw.video7) // Add all video resources here
        videoResId = allVideos.firstOrNull { !isVideoPlayed(it) && !isVideoInFolder(it, "liked") && !isVideoInFolder(it, "disliked") } ?: 0

        if (videoResId == 0) {
            // Show a message but don't close the app
            Toast.makeText(requireContext(), "No more videos to play", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createCustomFolders()
        videoFile = copyVideoToExternalStorage(videoResId, "video_$videoResId.mp4")

        playerView = view.findViewById(R.id.videoView)
        thumbsUpButton = view.findViewById(R.id.thumbsUpButton)
        thumbsDownButton = view.findViewById(R.id.thumbsDownButton)

        setupExoPlayer(videoFile!!)
        setupButtons(videoFile!!)
        resetButtonStates()
    }

    private fun setupExoPlayer(videoFile: File) {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(videoFile.toUri())
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
    }

    private fun resetButtonStates() {
        thumbsUpButton.setBackgroundResource(R.drawable.thumbs_up_border)
        thumbsDownButton.setBackgroundResource(R.drawable.ic_thumb_down)
        currentFolder = ""
    }

    private fun setupButtons(videoFile: File) {
        thumbsUpButton.setOnClickListener {
            if (currentFolder != "liked") {
                moveVideoToCustomFolder(videoFile, "liked")
                thumbsUpButton.setBackgroundResource(R.drawable.ic_thumb_up)
                thumbsDownButton.setBackgroundResource(R.drawable.ic_thumb_down)
                currentFolder = "liked"
                markVideoAsPlayed(videoResId)
                loadNextVideo()
            } else {
                Toast.makeText(requireContext(), "Video already in Liked folder", Toast.LENGTH_SHORT).show()
            }
        }

        thumbsDownButton.setOnClickListener {
            if (currentFolder != "disliked") {
                moveVideoToCustomFolder(videoFile, "disliked")
                thumbsDownButton.setBackgroundResource(R.drawable.ic_thumb_down)
                thumbsUpButton.setBackgroundResource(R.drawable.thumbs_up_border)
                currentFolder = "disliked"
                markVideoAsPlayed(videoResId)
                loadNextVideo()
            } else {
                Toast.makeText(requireContext(), "Video already in Disliked folder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNextVideo() {
        val allVideos = listOf(R.raw.video1, R.raw.video2, R.raw.video3, R.raw.video4, R.raw.video5, R.raw.video6, R.raw.video7) // Add all video resources here
        val nextVideoId = allVideos.firstOrNull {
            val videoId = it
            !isVideoPlayed(videoId) &&
                    !isVideoInFolder(videoId, "liked") &&
                    !isVideoInFolder(videoId, "disliked")
        }

        Log.d("ReelFragment", "Next video ID: $nextVideoId")

        if (nextVideoId != null) {
            val fragment = newInstance(nextVideoId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            // No more videos to play, just reset
            Toast.makeText(requireContext(), "No more videos to play", Toast.LENGTH_SHORT).show()
            resetButtonStates()
            setupExoPlayer(videoFile!!) // Reset the player view if no more videos
        }
    }


    private fun createCustomFolders() {
        val parentFolder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "ReelApp")
        if (!parentFolder.exists()) parentFolder.mkdirs()
        File(parentFolder, "liked").mkdirs()
        File(parentFolder, "disliked").mkdirs()
    }

    private fun moveVideoToCustomFolder(sourceFile: File, folderName: String) {
        val parentFolder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "ReelApp")
        val destinationFolder = File(parentFolder, folderName)
        destinationFolder.mkdirs()
        val destinationFile = File(destinationFolder, sourceFile.name)

        try {
            if (!destinationFile.exists()) sourceFile.copyTo(destinationFile, overwrite = true)
            if (sourceFile.exists()) sourceFile.delete()
            Toast.makeText(requireContext(), "Moved to $folderName folder", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FileMove", "Error moving file: ${e.message}")
        }
    }

    private fun copyVideoToExternalStorage(videoResId: Int, fileName: String): File {
        val parentFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ReelApp")
        parentFolder.mkdirs()
        val externalFile = File(parentFolder, fileName)
        if (!externalFile.exists()) {
            try {
                val inputStream = resources.openRawResource(videoResId)
                externalFile.outputStream().use { inputStream.copyTo(it) }
            } catch (e: Exception) {
                Log.e("FileCopy", "Error copying file: ${e.message}")
            }
        }
        return externalFile
    }

    private fun markVideoAsPlayed(videoId: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("PlayedVideos", Context.MODE_PRIVATE)
        val playedVideos = sharedPreferences.getStringSet("played", mutableSetOf()) ?: mutableSetOf()
        playedVideos.add(videoId.toString())
        sharedPreferences.edit().putStringSet("played", playedVideos).apply()
    }

    private fun isVideoPlayed(videoId: Int): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("PlayedVideos", Context.MODE_PRIVATE)
        val playedVideos = sharedPreferences.getStringSet("played", mutableSetOf())
        return playedVideos?.contains(videoId.toString()) == true
    }

    private fun isVideoInFolder(videoId: Int, folderName: String): Boolean {
        val parentFolder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "ReelApp")
        val folder = File(parentFolder, folderName)
        val videoFile = File(folder, "video_$videoId.mp4")
        return videoFile.exists()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
    }
}
