# Mutual Fund Excel Generator
Generates top 10 mutual funds in each category and prints in excel

Setup :
1. Java 21 - install from official website based on OS
2. Postman/insomnia
3. IDE - STS/Eclipse/IntelliJ

Running the springboot application
1. Clone the repository in your local using the command "git clone https://github.com/vaishsridh/MutualFundGenerator.git"
2. Open the repository in any IDE of your choice
3. Run the springboot application
4. Hit send and download "http://localhost:8080/download" on postman

How does it work ?
For each category (flexicap, smallcap and midcap), funds have been selected manually based on the age of the mutual fund ( > 5 years). Funds are available on https://www.amfiindia.com/spages/NAVAll.txt

Since we want historical data, we will be choosing regular growth mutual funds and not direct growth. Direct funds came into existence much later.
Direct and Regular funds of an AMC inherently share the same composition.

Each fund has a corresponding numeric code available in above text file. APIs are formed by this numeric code and gives us historical NAV (Ex: https://api.mfapi.in/mf/144548)
API urls are hardcoded in the application.

Generate combinations of 3 in each category and calculate average rolling returns, standard deviation and sharpe ratio based on latest inception date among 3 funds in a combination. Sharpe ratio is calculated as (average rolling returns - 7)/standard deviation.Choose the mutual fund in each combination with highest sharpe ratio among other other two.

Implement above process recursively and print top 10.

TODO : 
Short term :
1. Implement caching preferably by storing data in redis DB and refresh it every 24h at 11 PM IST
2. Data quality checks for missing NAVs. Present calculation includes 0 NAV.
3. Write junits
4. Enhance security - API urls are exposed as constants
5. Expand to ELSS category

Medium term :
1. Work on frontend to leverage the option of subscribing and adding to mailing list
2. Explore multithreading
3. Automate to mail excel to mail ids
4. Implement Holiday Calendar to not calculate on holidays
5. Host it on any server supporting springboot applications
6. Include expense ratio into account - Optional

Long term :
1. Enhance data quality by giving leeway to standard deviation and returns
2. Data collection is manual. It can be automated by considering funds with inception date > 5 years.
3. Expand to international funds
4. Exclude funds involved in front running or scam by crawling across the web - Optional

   
