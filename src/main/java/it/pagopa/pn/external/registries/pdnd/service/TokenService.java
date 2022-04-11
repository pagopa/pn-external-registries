package it.pagopa.pn.external.registries.pdnd.service;


import it.pagopa.pn.external.registries.pdnd.dto.TokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Scope("singleton")
@Slf4j
public class TokenService {
      private HashMap<String, TokenDto> tokensHolder =new HashMap<>();

      public TokenService()
      {
            String p="ANPR";
            TokenDto token = new TokenDto(p);
            tokensHolder.put(p,token);
      }
      public String getToken (String porpouseId)
      {
            TokenDto token;
            log.info("richiesta token per porpouseid -> "+ tokensHolder);
            boolean initializeToken=false;
            synchronized (tokensHolder) {
                 token = tokensHolder.get(porpouseId);
                  if (token == null) {
                        log.info("richiesta token per porpouseid -> "+ tokensHolder + " token nullo");
                        TokenDto t = new TokenDto(porpouseId);
                        tokensHolder.put(porpouseId, t);
                        initializeToken=true;
                  }
            }
            if (initializeToken)
            {
                  // TODO chiamata a PDND per richidere il token
                  //
                  try{
                        log.info("simulo chiamata a PDND per porpouseId {} ... wait",porpouseId);
                        Thread.sleep(50000);
                        log.info("ricevuta risposta da PDND per porpouseId {}",porpouseId);
                  }catch(Exception e)
                  {

                  }
            }
            return token.getBearerToken();

      }


}
