package com.example.demo.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import com.azure.storage.file.share.models.ShareFileUploadInfo;

public class CommonUtil {

	
	/**
	 * AzureFilesへのアップロード
	 * @param stream
	 * @param fileName
	 * @param accountName
	 * @param accountKey
	 */
	public static void azureFilesUplad(ByteArrayOutputStream stream, String fileName, String accountName, String accountKey, String shareName) {
		final String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + accountName + ";AccountKey=" + accountKey;
		
		// AzureFilesに接続
		ShareDirectoryClient dirClient = new ShareFileClientBuilder()
		         .connectionString(storageConnectionString)
		         .shareName(shareName)
		         .resourcePath("TEST_DIR_" + getLocalDateTimeJst("yyyyMMdd"))
		         .buildDirectoryClient();
		
		// ディレクトリの作成（なければ作成する）
		dirClient.createIfNotExists();
		
		
		// ファイルのアップロード
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(stream.toByteArray().length);
        ShareFileUploadInfo info = fileClient.uploadRange(new ByteArrayInputStream(stream.toByteArray()), stream.toByteArray().length);
        
        System.out.println("ETAG:" + info.getETag());
	}
	
	
	public static void azureFilesDownload(OutputStream stream, String dirName, String fileName, String accountName, String accountKey, String shareName) {
		final String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + accountName + ";AccountKey=" + accountKey;
		
		ShareDirectoryClient dirClient = new ShareFileClientBuilder()
	             .connectionString(storageConnectionString)
	             .shareName(shareName)
	             .resourcePath(dirName)
	             .buildDirectoryClient();

        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.download(stream);
	}
	
	
	/**
	 * 
	 * @param dateFormat
	 * @return
	 */
	public static String getLocalDateTimeJst(String dateFormat) {
		// JST時間
		return LocalDateTime.now(ZoneId.of("JST", ZoneId.SHORT_IDS)).format(DateTimeFormatter.ofPattern(dateFormat));
	}
}
