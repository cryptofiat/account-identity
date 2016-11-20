package com.kryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kryptoeuro.accountmapper.response.LdapResponse;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;

@Service
@Slf4j
public class LdapService {

	public LdapResponse lookupIdCode(long idCode) {

		LdapResponse lResponse = LdapResponse.builder().build();
		LdapNetworkConnection connection = new LdapNetworkConnection("ldap.sk.ee");
		try {
			connection.bind();
			EntryCursor cursor = connection.search("ou=Authentication,o=ESTEID,c=EE", "(serialNumber="+String.valueOf(idCode)+")", SearchScope.SUBTREE, "*");

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
			log.error("some excpetion" + e.toString());
		}

		return lResponse;
	}
}
