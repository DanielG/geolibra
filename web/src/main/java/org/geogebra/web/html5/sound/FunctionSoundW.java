package org.geogebra.web.html5.sound;


import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.main.App;
import org.geogebra.common.sound.FunctionSound;

/**
 * Class for playing function-generated sounds.
 * 
 * @author Laszlo Gal
 *
 */
public final class FunctionSoundW extends FunctionSound {
	public static final FunctionSoundW INSTANCE = new FunctionSoundW();


	/**
	 * Constructs instance of FunctionSound
	 * 
	 */
	public FunctionSoundW() {
		super();
		if (WebAudioWrapper.INSTANCE.init()) {
			App.debug("[WEB AUDIO] Initialization is OK.");
		} else {
			App.debug("[WEB AUDIO] Initialization has FAILED.");
		}

		if (!initStreamingAudio(getSampleRate(), getBitDepth())) {
			App.error("Cannot initialize streaming audio");
		}

	}
	/**
	 * Initializes instances of AudioFormat and SourceDataLine
	 * 
	 * @param sampleRate
	 *            = 8000, 16000, 11025, 16000, 22050, or 44100
	 * @param bitDepth
	 *            = 8 or 16
	 * @return
	 */
	protected boolean initStreamingAudio(int sampleRate, int bitDepth) {
		if (super.initStreamingAudio(sampleRate, bitDepth) == false) {
			return false;
		}
		
		boolean success = false;

		return success;
	}

	/**
	 * Plays a sound generated by the time valued GeoFunction f(t), from t = min
	 * to t = max in seconds. The function is assumed to have range [-1,1] and
	 * will be clipped to this range otherwise.
	 * 
	 * @param geoFunction
	 * @param min
	 * @param max
	 * @param sampleRate
	 * @param bitDepth
	 */

	@Override
	public void playFunction(final GeoFunction geoFunction, final double min,
			final double max, final int sampleRate, final int bitDepth) {
		if (!checkFunction(geoFunction, min, max, sampleRate, bitDepth)) {
			return;
		}
		App.debug("FunctionSound");
		WebAudioWrapper.INSTANCE.play();
		// close current sound thread and prepare sdl
		
		// spawn a new SoundThread to play the function sound
		
	}

	/**
	 * Pauses/resumes sound generation
	 * 
	 * @param doPause
	 */
	public void pause(boolean doPause) {

		if (doPause) {
			setMin(getT());
			stopSound();
		} else {
			playFunction(getF(), getMin(), getMax(), getSampleRate(),
					getBitDepth());
		}
	}

	private boolean stopped = false;



	private void generateFunctionSound() {

			stopped = false;

			// time between samples
			setSamplePeriod(1.0 / getSampleRate());

			// create internal buffer for mathematically generated sound data
			// a small buffer minimizes latency when the function changes
			// dynamically
			// TODO: find optimal buffer size
			int frameSetSize = getSampleRate() / 50; // 20ms ok?
			if (getBitDepth() == 8)
				setBuf(new byte[frameSetSize]);
			else
				setBuf(new byte[2 * frameSetSize]);

			// generate the function sound
				// open the sourceDataLine
				// TODO: the sdl buffer size is relative to our internal buffer
				// need to experiment for best sizing factor
		// sdl.open(af, 10 * getBufLength());
		// sdl.start();

				if (getBitDepth() == 16) {
					setT(getMin());
					loadBuffer16(getT());
					doFade(getBuf()[0], false);
			// sdl.write(getBuf(), 0, getBufLength());
					do {
						setT(getT() + getSamplePeriod() * frameSetSize);
						loadBuffer16(getT());
				// sdl.write(getBuf(), 0, getBufLength());
					} while (getT() < getMax() && !stopped);

					doFade(getBuf()[getBufLength() - 1], true);

				} else {
					// use 8-bit samples
					setT(getMin());
					loadBuffer8(getT());
					doFade(getBuf()[0], false);
			// sdl.write(getBuf(), 0, getBufLength());
					do {
						setT(getT() + getSamplePeriod() * frameSetSize);
						loadBuffer8(getT());
				// sdl.write(getBuf(), 0, getBufLength());
					} while (getT() < getMax() && !stopped);

					if (!stopped)
						doFade(getBuf()[getBufLength() - 1], true);

				}

				// finish transfer of bytes from internal buffer to the sdl
		// // buffer
		// sdl.drain();
		//
		// // stop and close the sourceDataLine
		// sdl.stop();
		// sdl.close();

	}
		/**
		 * Shapes ends of waveform to fade sound data TODO: is this actually
		 * working?
		 * 
		 * @param peakValue
		 * @param isFadeOut
		 */
		private void doFade(short peakValue, boolean isFadeOut) {

			byte[] fadeBuf = getFadeBuffer(peakValue, isFadeOut);
		// sdl.write(fadeBuf, 0, fadeBuf.length);
		}

		/**
		 * Stops function sound
		 */
		public void stopSound() {
			stopped = true;
		}

	}