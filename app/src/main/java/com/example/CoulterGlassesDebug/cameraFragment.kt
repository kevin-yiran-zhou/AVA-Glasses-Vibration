package com.example.CoulterGlassesDebug
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.CoulterGlassesDebug.databinding.CameraFragmentBinding
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.widget.IAspectRatio
import edu.wpi.first.math.MatBuilder
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.Nat
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Translation3d
import edu.wpi.first.math.numbers.N3

class cameraFragment() : CameraFragment() {
    private var mViewBinding: CameraFragmentBinding? = null
    private final val LOG_TAG = "CAMERA_FRAGMENT"

    data class FrameDimensions(val width: Int, val height: Int)
    val analysisCallBack = object : IPreviewDataCallBack {
        @SuppressLint("MissingPermission")
        override fun onPreviewData(
            data: ByteArray?,
            width: Int,
            height: Int,
            format: IPreviewDataCallBack.DataFormat
        ) {
//            Log.d(LOG_TAG, "Width: $width, Height: $height")
            if (data != null) {
            }
        }
    }

companion object{
    @JvmStatic
    public var frameDimensions: FrameDimensions? = FrameDimensions(320,240);//3840,2160
}

    // if you want offscreen render
    // please return null
    override fun getCameraView(): IAspectRatio? {
        return mViewBinding?.tvCameraRender
    }

    // if you want offscreen render
    // please return null
    override fun getCameraViewContainer(): LinearLayout? {
        return mViewBinding?.cameraViewContainer
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {addPreviewDataCallBack(analysisCallBack);}

    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(frameDimensions?.width!!)//320
            .setPreviewHeight(frameDimensions?.height!!)//240
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(true)
            .create()
    }
    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        if (mViewBinding == null) {
            mViewBinding = CameraFragmentBinding.inflate(inflater, container, false)
        }
        getCameraView()?.setAspectRatio(frameDimensions?.width!!, frameDimensions?.height!!);//320,240
        Log.d(LOG_TAG,"We are requesting analysis callback")
        addPreviewDataCallBack(analysisCallBack);
        return mViewBinding?.root
    }
}