Feature: Simple Purchases

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"
    * def findItemWithId =
    """
    function(arr, id) {
      if (arr === undefined || arr === null || id === undefined || id === null) {
        return null;
      }
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].id === id) {
          return arr[i];
        }
      }
      return null;
    }
    """

    * def calcPrice =
    """
    function (arr) {
      if (arr === undefined || arr === null) return 0.0;
      var res = 0.0
      for (var i = 0; i < arr.length; i++) {
        res += arr[i].amount * arr[i].pricePerUnit;
      }
      return res.toFixed(2);
    }
    """

    * def findVatDetailWithVatRate =
    """
    function(arr, vat) {
      if (arr === undefined || arr === null || vat === undefined || vat === null) {
        return null;
      }
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].vat === vat) {
          return arr[i];
        }
      }
      return null;
    }
    """

    * def defaultStockPostBody =
    """
    {
      name: "test",
      unitType: "PIECE",
      quantity: 140.0,
      pricePerUnit: 1.3,
      sustainablyProduced: true,
      originCategory: "UNKNOWN",
      producer: "producer",
      supplier: "supplier",
      stockStatus: "INSTOCK",
      vat: 0.19
    }
    """

    * def getUserIdFromToken =
    """
    function(token) {
        var base64Url = token.split('.')[1];
        var base64Str = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        var Base64 = Java.type('java.util.Base64');
        var decoded = Base64.getDecoder().decode(base64Str);
        var String = Java.type('java.lang.String');
        var decodedAsString = new String(decoded);
        var decodedAsObject = JSON.parse(decodedAsString);
        return decodedAsObject.id;
    }
    """

  Scenario: Purchase a single item
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Set Users balance
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', userId: '#uuid', name: '#string', balance: '#number', price: '#number', totalVat: '#number', vatDetails: '#array' }
    And assert response.name == "member"
    And assert response.price == 1.3
    And assert response.totalVat == 0.21
    And assert response.vatDetails.length == 1
    And assert response.vatDetails[0].vat == 0.19
    And assert response.vatDetails[0].amount == 0.21
    And def purchaseId = response.id

    # Check that stock was reduced
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check that the balance was reduced
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 498.7

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', userId: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in purchase list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And assert response.purchases.length == 1
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId), userId: '#uuid'}
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }
    And assert response.totalCumulativePrice == 1.3
    And assert response.totalCumulativeVat == 0.21

  Scenario: Purchase multiple items and check performed purchases using pagination
    # Get token
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Create item 3
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request
    """
    {
      name: "test",
      unitType: "PIECE",
      quantity: 140.0,
      pricePerUnit: 1.3,
      sustainablyProduced: true,
      originCategory: "UNKNOWN",
      producer: "producer",
      supplier: "supplier",
      stockStatus: "INSTOCK",
      vat: 0.16
    }
    """
    When method POST
    Then status 201
    And def stockId3 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Set members balance to 500
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Check number of purchases before
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And param offset = 0
    When method GET
    Then status 200
    And def purchaseCount = response.pagination.total

    # Purchase items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And def item3 = { id: #(stockId3), amount: 1 }
    And request { items: [#(item1), #(item2), #(item3)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number', totalVat: '#number', vatDetails: '#array' }
    And assert response.name == "member"
    And assert response.price == 3.9
    And assert response.totalVat == 0.6
    And assert response.vatDetails.length == 2
    And def vatRateOne = findVatDetailWithVatRate(response.vatDetails, 0.19)
    And match vatRateOne == { vat: 0.19, amount: 0.42 }
    And def vatRateTwo = findVatDetailWithVatRate(response.vatDetails, 0.16)
    And match vatRateTwo == { vat: 0.16, amount: 0.18 }
    And def purchaseId = response.id

    # Second purchase
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item4 = { id: #(stockId3), amount: 1 }
    And request { items: [#(item4)] }
    When method POST
    Then status 200

    # Third purchase
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item5 = { id: #(stockId3), amount: 1 }
    And request { items: [#(item5)] }
    When method POST
    Then status 200

    # Check stock was reduced on first item
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check stock was reduced on second item
    Given path '/stock/' + stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check that the balance was reduced (check integer cent value)
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert ~~(response.balance * 100) == 49350

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array', totalVat: '#number', vatDetails: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice
    And assert response.totalVat == 0.6
    And assert response.vatDetails.length == 2
    And def vatRateOne = findVatDetailWithVatRate(response.vatDetails, 0.19)
    And match vatRateOne == { vat: 0.19, amount: 0.42 }
    And def vatRateTwo = findVatDetailWithVatRate(response.vatDetails, 0.16)
    And match vatRateTwo == { vat: 0.16, amount: 0.18 }

    # Check that purchase exists in list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 3
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }
    And def purchasedItem2 = findItemWithId(purchase.items, stockId2)
    And match purchasedItem2 contains { id: #(stockId2) }

    # Check purchase list using pagination
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And param offset = 0
    And param limit = 2
    When method GET
    Then status 200
    And match response contains { pagination: '#object', purchases: '#array' }
    And match response.pagination == { offset: 0, limit: 2, total: '#number' }
    And match response.pagination.total == purchaseCount + 3
    And assert response.purchases.length == 2

  Scenario: Cannot purchase fractional items when unitType is PIECE
    # Login with orderer to create item
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Set members balance to 500
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1.5 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400008

    # Check quantity didn't change
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 140.0 }

    # Check quantity didn't change
    Given path '/stock/' + stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 140.0 }

    # Check that the balance wasn't reduced
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Cannot purchase deleted item
    # Get token for orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token
    And def userId = getUserIdFromToken(oToken)

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    And header Authorization = "Bearer " + oToken
    When method DELETE
    Then status 204

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Purchase items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400011

    # Check that the balance wasn't reduced
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Purchase a single item with higher amount than in stock is possible
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Set members balance to 500
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase Items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 200 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "member"
    And def purchaseId = response.id

    # Check quantity is below 0
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And assert response.quantity == -60

    # Check that the balance was reduced
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance < 500

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 1
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

  Scenario: Cannot have multiple items with same id in items array
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Set members balance to 500
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Purchase  items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item1AsWell = { id: #(stockId1), amount: 5 }
    And request { items: [#(item1), #(item1AsWell)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400010

    # Check that the balance wasn't reduced
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: The price of a purchase does not change after a stock items price was updated
    # Get token orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And def purchaseId = response.id

    # Get the purchase
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceFirstTime = response.totalPrice
    And def calculatedPriceFirstTime = calcPrice(response.items)

    # Update price of an item
    Given path '/stock/' + stockId1
    And header Authorization = "Bearer " + oToken
    And request { pricePerUnit: 5.0 }
    When method PATCH
    Then status 200

    # Get the purchase a second time
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceSecondTime = response.totalPrice
    And def calculatedPriceSecondTime = calcPrice(response.items)

    # Check prices are still the same
    And assert totalPriceFirstTime == totalPriceSecondTime
    And assert calculatedPriceFirstTime == calculatedPriceSecondTime

  Scenario: Request purchase via id from a different user fails
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Orderer purchases item
    Given path '/purchase'
    And header Authorization = "Bearer " + oToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "orderer"
    And assert response.price == 1.3
    And def purchaseId = response.id

    # Request purchase from different user
    Given path '/purchase', purchaseId
    And header Authorization = "Bearer " + mToken
    When method GET
    Then status 401
    And assert response.errorCode == 401001

  Scenario: GET /purchase requires authorization
    Given path '/purchase'
    When method GET
    Then status 401
    And assert response.errorCode == 401005

  Scenario: POST /purchase requires authorization
    Given path '/purchase'
    And def item1 = { id: "13246", amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 401
    And assert response.errorCode == 401005

  Scenario: GET /purchase/{purchaseId} requires authorization
    Given path '/purchase', "132456"
    When method GET
    Then status 401
    And assert response.errorCode == 401005

  Scenario: Purchasing an item with stockStatus OUTOFSTOCK is not possible
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item that is out of stock
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request
    """
    {
      name: "test",
      unitType: "PIECE",
      quantity: 140.0,
      pricePerUnit: 5.0,
      sustainablyProduced: true,
      originCategory: "UNKNOWN",
      producer: "producer",
      supplier: "supplier",
      stockStatus: "OUTOFSTOCK",
      vat: 0.19
    }
    """
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item that is in stock
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And match response.errorCode == 400021

    # Check that item's quantity did not change
    Given path 'stock', stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == 140.0

    # Check that item's quantity did not change
    Given path 'stock', stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == 140.0

  Scenario: Purchasing an item with stockStatus ORDERED is not possible
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item that is ORDERED
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request
    """
    {
      name: "test",
      unitType: "PIECE",
      quantity: 140.0,
      pricePerUnit: 5.0,
      sustainablyProduced: true,
      originCategory: "UNKNOWN",
      producer: "producer",
      supplier: "supplier",
      stockStatus: "ORDERED",
      vat: 0.19
    }
    """
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item that is INSTOCK
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And match response.errorCode == 400019

    # Check that item's quantity did not change
    Given path 'stock', stockId1
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == 140.0

    # Check that item's quantity did not change
    Given path 'stock', stockId2
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.quantity == 140.0

  Scenario: Purchase list with multiple purchases
    # Create Item with Orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Login with member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Get current purchase list details
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And def currentPurchases = response.purchases.length
    And def currentTotalCumulativePrice = response.totalCumulativePrice
    And def currentTotalCumulativeVat = response.totalCumulativeVat

    # Purchase 1
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', userId: '#uuid', name: '#string', balance: '#number', price: '#number', totalVat: '#number', vatDetails: '#array' }

    # Purchase 2
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 2}
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', userId: '#uuid', name: '#string', balance: '#number', price: '#number', totalVat: '#number', vatDetails: '#array' }

    # Check purchase list
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And assert response.purchases.length == currentPurchases + 2
    And match response.totalCumulativePrice.toFixed(2) == (currentTotalCumulativePrice + 3.9).toFixed(2)
    And match response.totalCumulativeVat.toFixed(2) == (currentTotalCumulativeVat + 0.63).toFixed(2)

  Scenario: Purchasing an item with no price is not possible
    # Get token for orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request
    """
    {
      name: "test",
      unitType: "PIECE",
      quantity: 140.0,
      sustainablyProduced: true,
      originCategory: "UNKNOWN",
      producer: "producer",
      supplier: "supplier",
      stockStatus: "INSTOCK",
      vat: 0.19
    }
    """
    When method POST
    Then status 201
    And def stockId = response.id

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Purchase item
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 400
    And match response.errorCode == 400025
