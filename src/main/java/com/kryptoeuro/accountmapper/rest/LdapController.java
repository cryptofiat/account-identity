package com.kryptoeuro.accountmapper.rest;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import com.kryptoeuro.accountmapper.response.LdapResponse;
import com.kryptoeuro.accountmapper.service.LdapService;
import com.kryptoeuro.accountmapper.error.LdapNotFoundException;
import com.kryptoeuro.accountmapper.error.SearchTooBroadException;
import com.kryptoeuro.accountmapper.LdapResponseRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@RestController
@RequestMapping("/v1/ldap")
@Slf4j
@CrossOrigin(origins = "*")
public class LdapController {

	@Autowired
	LdapService ldapService;
	@Autowired
	LdapResponseRepository ldapResponseRepository;

	private static int MIN_SEARCH_QUERY_LENGTH = 3;
	private static int MAX_RESULT_SIZE = 10;

	@RequestMapping(method = RequestMethod.GET, value = "/{idCode}")
	public ResponseEntity<LdapResponse> checkIdCode(@PathVariable("idCode") long idCode) {
		LdapResponse lr = ldapService.lookupIdCode(idCode);
		// should check if didn't return, then respond with 404
		if (lr != null && lr.getIdCode() > 0) {
			return new ResponseEntity<LdapResponse>(lr, HttpStatus.OK);
		} else {
			throw new LdapNotFoundException();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search")
	public ResponseEntity<List<LdapResponse>> search( @RequestParam("searchString") String searchString) {
		if ( searchString.length() < MIN_SEARCH_QUERY_LENGTH) {
			throw new SearchTooBroadException();
		}

		List<LdapResponse> lr = ldapResponseRepository.searchLdapResponse(searchString, new PageRequest(0,MAX_RESULT_SIZE));
		return new ResponseEntity<List<LdapResponse>>(lr, HttpStatus.OK);
	}
}
