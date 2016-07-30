package com.kryptoeuro.accountmapper.mobileid;

import com.codeborne.security.AuthenticationException;
import com.codeborne.security.mobileid.MobileIDAuthenticator;
import com.codeborne.security.mobileid.MobileIDSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class MobileIdAuthService {

	private static final String TEST_DIGIDOC_SERVICE_URL = "https://tsp.demo.sk.ee/";
	private MobileIDAuthenticator mid = new MobileIDAuthenticator(TEST_DIGIDOC_SERVICE_URL);

	public MobileIDSession login(String phoneNumber) {
		MobileIDSession mobileIDSession;
		try {
//			log.debug("Mobile ID authentication with challenge " + mobileIDSession.challenge + " sent to " + phoneNumber);
			mobileIDSession = mid.startLogin(phoneNumber);
			mid.waitForLogin(mobileIDSession);
//			log.debug("Mobile ID authentication success! First name: " + mobileIDSession.firstName + ", Last name: " + mobileIDSession.lastName + ", Personal code: " + mobileIDSession.personalCode);

		} catch (AuthenticationException e) {
			e.printStackTrace();
			return null;
//			log.debug("Mobile ID authentication failed" + e.getMessage());
		}

		return mobileIDSession;
	}



}
