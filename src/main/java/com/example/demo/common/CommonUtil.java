package com.example.demo.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
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
		         .resourcePath("TEST_DIR_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
		         .buildDirectoryClient();
		
		// ディレクトリの作成（なければ作成する）
		dirClient.createIfNotExists();
		
		
		// ファイルのアップロード
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(stream.toByteArray().length);
        ShareFileUploadInfo info = fileClient.uploadRange(new ByteArrayInputStream(stream.toByteArray()), stream.toByteArray().length);
        
        System.out.println("ETAG:" + info.getETag());
	}
}
