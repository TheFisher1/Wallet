# CryptoCurrencyWallet 

The Wallet app is a command-line tool that allows users to invest in certain crypto cryptocurrencies. Moreover, it allows users to deposit and withdraw money, as well see the current trends in the crypto assets that are offered. The information is stored on .txt files on the server and it will be retrieved in the event of server's restart. As for the assets, an http request is sent every 30 minutes that retrieves the currencies from a separate scheduled threadpool which ensures that prices are up to date.  

# Commands
Command               | Parameters |
:-------------------: | :----------:
register |  \<username\> \<password\>
log-in   | \<username\> \<password\>
deposit  | \<amount $\>
withdraw | \<amount $\>
balance  | -
list-offerings | -
buy-asset | \<assetId\> \<amount $\>
sell-asset | \<assetId\>
get-summary | -
get-overall-summary | -
log-out | -
quit | -
