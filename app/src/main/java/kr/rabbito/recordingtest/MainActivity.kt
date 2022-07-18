package kr.rabbito.recordingtest

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kr.rabbito.recordingtest.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private val RECORD_REQUEST = 100
    private var storagePath: String? = null

    private var recorder: ExtAudioRecorder? = null
    
    private val wavFiles = ArrayList<String>()
    private var selectedFile: String? = null
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var mPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 안드로이드 버전에 따른 저장 위치 지정
        setStoragePath()
        Log.d("Storage Path", storagePath!!) // 저장 경로

        // 권한 확인 및 요청
        val permissions = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        checkPermissions(permissions, RECORD_REQUEST)

        // 파일 불러오기
        loadFiles()
        // 리스트뷰에 불러온 파일 대입
        setListView(binding.mainClBotLvFiles)

        // 녹음 시작
        binding.mainClTopBtnRecord.setOnClickListener {
            val fileName = "test${wavFiles.size + 1}"
            startRecording(fileName)
        }

        // 녹음 중지
        binding.mainClTopBtnStop.setOnClickListener {
            stopRecording()
            // 파일 다시 불러오기
            loadFiles()
            adapter.notifyDataSetChanged()
        }

        // 재생
        binding.mainClBotBtnPlay.setOnClickListener {
            // 선택한 파일 재생
            startPlaying(selectedFile)
        }

        // 중지
        binding.mainClBotBtnStop.setOnClickListener {
            stopPlaying()
        }

        // 모두 삭제
        binding.mainClBotBtnRemoveAll.setOnClickListener {
            removeAllFiles()
            loadFiles()
            adapter.notifyDataSetChanged()
        }
    }

    private fun setStoragePath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            storagePath = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!.path
        } else {
            storagePath = Environment.getExternalStorageDirectory().path
        }
    }

    private fun checkPermissions(permissions: Array<String>, code: Int) {
        if (permissions.any {
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, code)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_REQUEST) {
            if (grantResults.isNotEmpty()) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadFiles() {
        wavFiles.clear()

        val listFiles = File(storagePath!!).listFiles()
        for (file in listFiles!!) {
            val fileName = file.name
            val extName = fileName.substring(fileName.length - 3)
            // wav 파일만 저장
            if (extName == "wav")
                wavFiles.add(fileName)
        }

        if (wavFiles.size > 0) {
            selectedFile = wavFiles[0]
        }
    }

    private fun setListView(listView: ListView) {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, wavFiles)

        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        // 어댑터 연결
        listView.adapter = adapter
        listView.setItemChecked(0, true)
        listView.setOnItemClickListener { adapterView, view, i, l ->
            selectedFile = wavFiles[i]
        }
    }

    private fun startRecording(fileName: String) {
        // WAV 포맷으로 저장하는 ExtAudioRecorder 객체 생성
        recorder = ExtAudioRecorder.getInstanse(false)
        recorder!!.setOutputFile("$storagePath/$fileName.wav")

        // 녹음 시작
        recorder!!.prepare()
        recorder!!.start()

        binding.mainClTopBtnStop.isEnabled = true
        binding.mainClTopBtnRecord.isEnabled = false
    }

    private fun stopRecording() {
        // 녹음 중지
        recorder!!.stop()
        recorder!!.release()

        binding.mainClTopBtnStop.isEnabled = false
        binding.mainClTopBtnRecord.isEnabled = true
    }

    private fun startPlaying(fileName: String?) {
        if (wavFiles.size > 0) {
            mPlayer = MediaPlayer()
            mPlayer.setDataSource("$storagePath/$fileName")

            // 재생 시작
            mPlayer.prepare()
            mPlayer.start()

            // 파일이 끝까지 재생된 경우
            mPlayer.setOnCompletionListener {
                stopPlaying()
            }

            binding.mainClBotBtnStop.isEnabled = true
            binding.mainClBotBtnPlay.isEnabled = false
        } else {
            Toast.makeText(this, "No File", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlaying() {
        // 재생 중지
        mPlayer.stop()
        mPlayer.reset()

        binding.mainClBotBtnStop.isEnabled = false
        binding.mainClBotBtnPlay.isEnabled = true
    }

    private fun removeAllFiles() {
        val listFiles = File(storagePath!!).listFiles()
        for (file in listFiles) {
            file.delete()
        }
    }
}