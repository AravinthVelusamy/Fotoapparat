package io.fotoapparat.hardware.v1;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import io.fotoapparat.hardware.CameraDevice;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.hardware.Parameters;
import io.fotoapparat.photo.Photo;

/**
 * Camera hardware driver for v1 {@link Camera} API.
 */
@SuppressWarnings("deprecation")
public class Camera1 implements CameraDevice {

	private Camera camera;

	@Override
	public void open(Parameters parameters) {
		try {
			// TODO pick lens position
			camera = Camera.open();
		} catch (RuntimeException e) {
			throw new CameraException(e);
		}

		// TODO apply parameters

		camera.setErrorCallback(new Camera.ErrorCallback() {
			@Override
			public void onError(int error, Camera camera) {
				throw new IllegalStateException("Camera error code: " + error);
			}
		});
	}

	@Override
	public void close() {
		if (camera != null) {
			camera.release();
		}
	}

	@Override
	public void startPreview() {
		camera.startPreview();
	}

	@Override
	public void stopPreview() {
		camera.stopPreview();
	}

	@Override
	public void setDisplaySurface(Object displaySurface) {
		try {
			trySetDisplaySurface(displaySurface);
		} catch (IOException e) {
			throw new CameraException(e);
		}
	}

	private void trySetDisplaySurface(Object displaySurface) throws IOException {
		if (displaySurface instanceof SurfaceTexture) {
			camera.setPreviewTexture(((SurfaceTexture) displaySurface));
		} else if (displaySurface instanceof SurfaceHolder) {
			camera.setPreviewDisplay(((SurfaceHolder) displaySurface));
		} else {
			throw new IllegalArgumentException("Unsupported display surface: " + displaySurface);
		}
	}

	@Override
	public Photo takePicture() {
		final CountDownLatch latch = new CountDownLatch(1);

		camera.takePicture(
				null,
				null,
				null,
				new Camera.PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						// TODO take result
						// TODO check current screen orientation
						latch.countDown();
					}
				}
		);

		try {
			latch.await();
		} catch (InterruptedException e) {
			// Do nothing
		}

		return null;
	}

}
