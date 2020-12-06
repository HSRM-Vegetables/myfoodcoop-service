Feature: A simple Karate Test for the Hello World Controller

  Background:
    * url baseUrl

  Scenario: GET returns correct message
    Given path '/hello-world'
    When method GET
    Then assert response.message === "Hello World!"
