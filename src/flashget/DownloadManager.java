package flashget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Observable;

import javax.management.RuntimeErrorException;

/**
 * Manage Thread and Downloading
 * @author Tharit Pongsaneh
 *
 */
public class DownloadManager extends Observable {

	private final int BUFFERSIZE = 4096;
	private String filename;
	private String urlName;
	private URL url;
	private long filesize;
	private int threadSize;
	private long currentSize = 0;
	private ThreadGroup GroupThread;
	private Thread thread1 = new Thread();
	private Thread thread2 = new Thread();
	private Thread thread3 = new Thread();
	private Thread thread4 = new Thread();
	private Thread thread5 = new Thread();
	private ThreadDownload download1;
	private ThreadDownload download2;
	private ThreadDownload download3;
	private ThreadDownload download4;
	private ThreadDownload download5;

	
	public String getFilename() {
		return filename;
	}

	public String getUrlName() {
		return urlName;
	}

	public long getFilesize() {
		return filesize;
	}

	/**
	 * Set URL
	 * @param urlName set URL and Important Info
	 */
	public void setURL(String urlName) {
		this.urlName = urlName;
		try {
			url = new URL(this.urlName);
			filename = url.getFile().substring(url.getFile().lastIndexOf('/') + 1, url.getFile().length());
			URLConnection connection = url.openConnection();
			filesize = connection.getContentLengthLong();
			threadSize = (int) (filesize / 5);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	/**
	 * Downlaod File
	 * @throws InterruptedException
	 */
	public void download() throws InterruptedException {
		download1 = new ThreadDownload(0, this.threadSize);
		download2 = new ThreadDownload(this.threadSize, this.threadSize);
		download3 = new ThreadDownload(this.threadSize * 2, this.threadSize);
		download4 = new ThreadDownload(this.threadSize * 3, this.threadSize);
		download5 = new ThreadDownload(this.threadSize * 4, this.threadSize);

		thread1 = new Thread(GroupThread, download1);
		thread2 = new Thread(GroupThread, download2);
		thread3 = new Thread(GroupThread, download3);
		thread4 = new Thread(GroupThread, download4);
		thread5 = new Thread(GroupThread, download5);

		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();
		thread5.start();

		thread1.join();
		thread2.join();
		thread3.join();
		thread4.join();
		thread5.join();

	}

	
	public long getCurrentSize() {
		return currentSize;
	}

	public void cancle() {
		GroupThread.interrupt();
	}
	
	/**
	 * Runnable Class For Thread
	 * @author Tharit Pongsaneh
	 *
	 */
	class ThreadDownload implements Runnable {

		private int start;
		private int size;
		private InputStream instream;

		public ThreadDownload(int start, int size) {
			this.start = start;
			this.size = size;
		}

		/**
		 * Set up connection
		 */
		private void setup() {
			try {
				URLConnection connection = url.openConnection();
				String range = null;
				if (size > 0) {
					range = String.format("bytes=%d-%d", start, start + size - 1);
				} else {
					range = String.format("bytes=%d-", start);
				}
				connection.setRequestProperty("Range", range);
				instream = connection.getInputStream();
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}

		/**
		 * Write File
		 */
		private void write() {
			File file = new File(filename);
			RandomAccessFile writer = null;
			try {
				writer = new RandomAccessFile(file, "rwd");
			} catch (FileNotFoundException e) {
				throw new RuntimeException();
			}
			try {
				writer.seek(start);
			} catch (IOException e) {
				throw new RuntimeException();
			}
			int buffersize = Math.min(size, BUFFERSIZE);
			byte[] buffer = new byte[buffersize];
			int bytesRead = 0;
			try {
				do {
					int n = instream.read(buffer);
					if (n < 0)
						break;
					writer.write(buffer, 0, n);
					bytesRead += n;
					currentSize += n;
					setChanged();
					notifyObservers();
				} while (bytesRead < size || !Thread.interrupted());
			} catch (IOException e) {
				throw new RuntimeException();
			} finally {
				try {
					if (instream != null)
						instream.close();
					writer.close();
				} catch (IOException e) {
					/** ignore **/
				}
			}
		}

		@Override
		public void run() {
			setup();
			write();
		}
	}

}
