package eu.cryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.cryptoeuro.accountmapper.response.LdapResponse;
import eu.cryptoeuro.accountmapper.LdapResponseRepository;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import java.util.List;

@Service
@Slf4j
public class LdapService {

	@Autowired
	LdapResponseRepository ldapResponseRepository;

	private LdapResponse tryLocalCache(long idCode) {
		List<LdapResponse> responseList = ldapResponseRepository.findByIdCode(Long.valueOf(idCode));
		return (responseList.isEmpty()) ? null : responseList.get(0);
        }
	private void storeLocalCache(LdapResponse ldapResponse) {
		try {
			ldapResponseRepository.save(ldapResponse);
		} catch (Exception e) {
			log.warn("Could not store LDAP response in local DB for ID: " + ldapResponse.toString(), e);
		}
        }

	public LdapResponse lookupIdCode(long idCode) {

		LdapResponse lResponse = LdapResponse.builder().build();
		lResponse = tryLocalCache(idCode);

		if (lResponse != null && lResponse.getIdCode() > 0) {
			return lResponse;
		}

		LdapNetworkConnection connection = new LdapNetworkConnection("ldap.sk.ee");
		try {
			connection.bind();
			EntryCursor cursor = connection.search("c=EE", "(serialNumber="+String.valueOf(idCode)+")", SearchScope.SUBTREE, "*");

			while (cursor.next()) {
				Entry entry = cursor.get();
				log.info("got an entry: "+entry.toString());
				String cn = entry.get("cn").getString();
				lResponse = LdapResponse.builder()
							.idCode(Long.valueOf(idCode))
							.firstName(cn.split(",")[1])
							.lastName(cn.split(",")[0])
							.build();
			}

			connection.unBind();
			connection.close();
		} catch (Exception e) {
			log.error("Exception trying LDAP " + e.toString());
		}


		if (lResponse != null && lResponse.getIdCode() > 0) {
			storeLocalCache(lResponse);
			return lResponse;
		} else {
			return null; 
		}

	}
}
