package eu.cryptoeuro.accountmapper.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Arrays;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

import eu.cryptoeuro.accountmapper.response.WalletServerGasPriceResponse;
import eu.cryptoeuro.accountmapper.response.WalletServerAccountResponse;
import eu.cryptoeuro.accountmapper.response.WalletServerHistoryResponse;

@Service
@Slf4j
public class WalletServerService {

	@Autowired
	EthereumService ethService;

	@Value("${wallet.server.url}")
	private String walletServer;

	@Value("${ref.server.url}")
	private String refServer;

	public WalletServerAccountResponse getAccount(String address) {

		ObjectMapper mapper = new ObjectMapper();
		WalletServerAccountResponse obj = null; 
		try { 
			obj = mapper.readValue(new URL(walletServer+"/v1/accounts/"+address), WalletServerAccountResponse.class);
		} catch (Exception e) {
			log.error("Failed loading account data from wallet-server", e);
		}
		return obj;
	}

	public List<WalletServerHistoryResponse> getHistory(String address) {
		
		ObjectMapper mapper = new ObjectMapper();
		WalletServerHistoryResponse[] obj = null; 
		try { 
			obj = mapper.readValue(new URL(walletServer+"/v1/accounts/"+address+"/transfers"), WalletServerHistoryResponse[].class);
		
	  	    for (WalletServerHistoryResponse resp : obj) {
			log.info("History: tx " + resp.getId() + " from " + resp.getSourceAccount());
		    }
		} catch (Exception e) {
			log.error("Failed loading history data from wallet-server", e);
		}
		return Arrays.asList(obj);
	}

	public void copyReference(String fromTransactionHash, String toTransactionHash) throws MalformedURLException, JSONException {

		try {
			JSONObject refJson = new JSONObject(IOUtils.toString(new URL(refServer+ethService.without0x(fromTransactionHash)), Charset.forName("UTF-8")));

			  byte[] postDataBytes = refJson.toString().getBytes("UTF-8");

			  URL url = new URL(refServer+ethService.without0x(toTransactionHash));
			  HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			  httpCon.setRequestMethod("POST");
		          httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		          httpCon.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			  httpCon.setDoOutput(true);
       			  httpCon.getOutputStream().write(postDataBytes);

			  OutputStreamWriter out = new OutputStreamWriter(
			      httpCon.getOutputStream());
			  log.info("Putting ref: "+httpCon.getResponseCode()+ " msg: " + httpCon.getResponseMessage());
			  out.close();
			log.info("got this ref json: " + refJson.toString());
		} catch (IOException io) {
			log.warn("IO excpetion in reading reference, probably doesn't exist for tx: "+fromTransactionHash);
		}
		
	}

	public Long getGasPriceWei() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		WalletServerGasPriceResponse response;
		try {
			response = mapper.readValue(new URL(walletServer+"/v1/fees/gasPrice"), WalletServerGasPriceResponse.class);
		} catch (Exception e) {
			log.error("Failed loading gas price from wallet-server", e);
			return ethService.getGasPriceWeiFromNetwork();
		}
		return response.getGasPriceWei();
	}
}
