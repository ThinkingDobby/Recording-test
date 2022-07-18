package kr.rabbito.recordingtest

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kr.rabbito.recordingtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private val RECORD_REQUEST = 100
    private var storagePath: String? = null

    private var recorder: ExtAudioRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            storagePath = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!.path
        } else {
            storagePath = Environment.getExternalStorageDirectory().path
        }
        Log.d("Storage Path", storagePath!!) // 저장 경로

        val permissions = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        // 녹음 위한 권한 요청
        checkPermissions(permissions, RECORD_REQUEST)

        binding.mainClTopBtnRecord.setOnClickListener {
            // WAV 포맷으로 저장하는 ExtAudioRecorder 객체 생성
            recorder = ExtAudioRecorder.getInstanse(false)
            recorder!!.setOutputFile("$storagePath/test1.wav")
            // 녹음 시작
            recorder!!.prepare()
            recorder!!.start()
        }

        binding.mainClTopBtnStop.setOnClickListener {
            recorder!!.stop()
            recorder!!.release()
            recorder = null
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
}