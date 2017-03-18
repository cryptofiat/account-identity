import eu.cryptoeuro.accountmapper.service.EthereumService;

import java.io.IOException;

public class EthereumServiceSample {

  public static void main(String[] args) throws IOException {
    EthereumService ethereumService = new EthereumService();
    ethereumService.activateEthereumAccount("0xdcc4d964ca07022d4ce46ba97d3ce88544c04f66");
  }
}
