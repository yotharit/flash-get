package flashget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.Observable;


/**
 * Manage Thread and Downloading
 * 
 * @author Tharit Pongsaneh
 *
 */
public class DownloadManager extends Observable {

	//Attributes for this class
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

	/**
	 * GET filename
	 * @return the name of downloaded file
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Get Url
	 * @return url that we download file from
	 */
	public String getUrlName() {
		return urlName;
	}

	/**
	 * Get file size
	 * @return download size
	 */
	public long getFilesize() {
		return filesize;
	}

	/**
	 * Set URL and information
	 * 
	 * @param urlName
	 *            set URL and Important Info
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
	 * Downlaod File by thread
	 * 
	 * @throws InterruptedException
	 */
	public void download() throws InterruptedException {
		GroupThread = new ThreadGroup("DOWNLOAD THREAD");
		download1 = new ThreadDownload(0, this.threadSize);
		download2 = new ThreadDownload(this.threadSize, this.threadSize);
		download3 = new ThreadDownload(this.threadSize * 2, this.threadSize);
		download4 = new ThreadDownload(this.threadSize * 3, this.threadSize);
		download5 = new ThreadDownload(this.threadSize * 4, this.threadSize);

		thread1 = new Thread(GroupThread, download1, "Download 1");
		thread2 = new Thread(GroupThread, download2, "Download 2");
		thread3 = new Thread(GroupThread, download3, "Download 3");
		thread4 = new Thread(GroupThread, download4, "Download 4");
		thread5 = new Thread(GroupThread, download5, "Download 5");

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

	/**
	 * Interrupt Thread and delete files
	 */
	public void cancle() {
		GroupThread.interrupt();
		File file = new File(filename);
		file.delete();
	}

	/**
	 * Runnable Class For Thread
	 * 
	 * @author Tharit Pongsaneh
	 *
	 */
	class ThreadDownload implements Runnable {

		private int start;
		private int size;
		private InputStream instream;
		private RandomAccessFile writer;
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
			writer = null;
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
				} while (bytesRead < size && !Thread.interrupted());
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
		private void close(){
			try {
				instream.close();
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}
		
		@Override
		public void run() {
			setup();
			write();
			close();
		}
	}

}
