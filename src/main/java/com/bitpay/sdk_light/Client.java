package com.bitpay.sdk_light;

import com.bitpay.sdk_light.exceptions.*;
import com.bitpay.sdk_light.model.Bill.Bill;
import com.bitpay.sdk_light.model.Invoice.Invoice;
import com.bitpay.sdk_light.model.Rate.Rate;
import com.bitpay.sdk_light.model.Rate.Rates;
import com.bitpay.sdk_light.util.BitPayLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
/**
 * <img src="https://bitpay.com/_nuxt/img/1c0494b.svg" width="120" alt="BitPay">
 * <h1>BitPay Java light client</h1>
 * Light implementation of the client for the BitPay's Cryptographically Secure RESTful API.
 * <p>
 * See <a href="https://bitpay.com/docs">bitpay.com/docs</a> for more information.
 *
 * @author Antonio Buedo
 * @version 1.6.2004
 * @since 15.04.2020
 */

public class Client {

    private static BitPayLogger _log = new BitPayLogger(BitPayLogger.OFF);
    private String _env;
    private String _token;
    private String _baseUrl;
    private HttpClient _httpClient = null;

    /**
     * Constructor for use if the keys and SIN are managed by this library.
     *
     * @param token The token generated on the BitPay account.
     * @throws BitPayException BitPayException class
     */
    public Client(String token) throws BitPayException {
        try {
            this._token = token;
            this._env = Env.Prod;
            this.init();
        } catch (Exception e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Config) : " + e.getMessage());
        }
    }

    /**
     * Constructor for use if the keys and SIN are managed by this library.
     *
     * @param token The token generated on the BitPay account.
     * @param environment The target environment [Default: Production].
     * @throws BitPayException BitPayException class
     */
    public Client(String token, String environment) throws BitPayException {
        try {
            this._token = token;
            this._env = environment;
            this.init();
        } catch (Exception e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Config) : " + e.getMessage());
        }
    }

    /**
     * Initialize this object with the client name and the environment Url.
     *
     * @throws BitPayException BitPayException class
     */
    private void init() throws BitPayException {
        try {
            this._baseUrl = this._env.equals(Env.Test) ? Env.TestUrl : Env.ProdUrl;
            _httpClient = HttpClientBuilder.create().build();
        } catch (Exception e) {
            throw new BitPayException(null, "Error - failed to build configuration : " + e.getMessage());
        }
    }

    /**
     * Create a BitPay invoice using the Merchant facade.
     *
     * @param invoice An Invoice object with request parameters defined.
     * @return A BitPay generated Invoice object.
     * @throws BitPayException BitPayException class
     * @throws InvoiceCreationException InvoiceCreationException class
     */
    public Invoice createInvoice(Invoice invoice) throws BitPayException, InvoiceCreationException {
        invoice.setToken(this._token);
        invoice.setGuid(this.getGuid());

        ObjectMapper mapper = new ObjectMapper();

        String json;

        try {
            json = mapper.writeValueAsString(invoice);
        } catch (JsonProcessingException e) {
            throw new InvoiceCreationException(null, "failed to serialize Invoice object : " + e.getMessage());
        } 

        try {
        	HttpResponse response = this.post("invoices", json);
            invoice = mapper.readerForUpdating(invoice).readValue(this.responseToJsonString(response));
        } catch (BitPayException ex) {
            throw new InvoiceCreationException(ex.getStatusCode(), ex.getReasonPhrase());
        } catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }

        return invoice;
    }

    /**
     * Retrieve a BitPay invoice by invoice id using the public facade.
     *
     * @param invoiceId The id of the invoice to retrieve.
     * @return A BitPay Invoice object.
     * @throws BitPayException BitPayException class
     * @throws InvoiceQueryException InvoiceQueryException class
     */
    public Invoice getInvoice(String invoiceId) throws BitPayException, InvoiceQueryException {
        final List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", this._token));

        Invoice invoice;
        try {
        	HttpResponse response = this.get("invoices/" + invoiceId, params);
            invoice = new ObjectMapper().readValue(this.responseToJsonString(response), Invoice.class);
        } catch (BitPayException ex) {
            throw new InvoiceQueryException(ex.getStatusCode(), ex.getReasonPhrase());
        } catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }

        return invoice;
    }

    /**
     * Retrieve the exchange rate table maintained by BitPay.  See https://bitpay.com/bitcoin-exchange-rates.
     *
     * @return A Rates object populated with the BitPay exchange rate table.
     * @throws BitPayException BitPayException class
     * @throws RateQueryException RateQueryException class
     */
    public Rates getRates() throws BitPayException, RateQueryException {
        List<Rate> rates;
        try {
        	HttpResponse response = this.get("rates");
            rates = Arrays.asList(new ObjectMapper().readValue(this.responseToJsonString(response), Rate[].class));
        } catch (BitPayException ex) {
            throw new RateQueryException(ex.getStatusCode(), ex.getReasonPhrase());
        } catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Rates) : " + e.getMessage());
        }

        return new Rates(rates, this);
    }

    /**
     * Create a BitPay bill using the POS facade.
     *
     * @param bill An Bill object with request parameters defined.
     * @return A BitPay generated Bill object.
     * @throws BitPayException BitPayException class
     * @throws BillCreationException BillCreationException class
     */
    public Bill createBill(Bill bill) throws BitPayException, BillCreationException {
    	String token = this._token;
        bill.setToken(token);
        ObjectMapper mapper = new ObjectMapper();
        String json;

        try {
            json = mapper.writeValueAsString(bill);
        } catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to serialize Bill object : " + e.getMessage());
        }

        try {
        	HttpResponse response = this.post("bills", json);
            bill = mapper.readerForUpdating(bill).readValue(this.responseToJsonString(response));
        } catch (BitPayException ex) {
            throw new BillCreationException(ex.getStatusCode(), ex.getReasonPhrase());
        } catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Bill) : " + e.getMessage());
        }

        return bill;
    }

    /**
     * Retrieve a BitPay bill by bill id using the public facade.
     *
     * @param billId The id of the bill to retrieve.
     * @return A BitPay Bill object.
     * @throws BitPayException BitPayException class
     * @throws BillQueryException BillQueryException class
     */
    public Bill getBill(String billId) throws BitPayException, BillQueryException {
        String token = this._token;
        final List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", token));
        
        Bill bill;
        try {
        	HttpResponse response = this.get("bills/" + billId, params);
            bill = new ObjectMapper().readValue(this.responseToJsonString(response), Bill.class);
        } catch (BitPayException ex) {
            throw new BillQueryException(ex.getStatusCode(), ex.getReasonPhrase());
        } catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to deserialize BitPay server response (Bill) : " + e.getMessage());
        }

        return bill;
    }

    /**
     * Deliver a BitPay Bill.
     *
     * @param billId    The id of the requested bill.
     * @param billToken The token of the requested bill.
     * @throws BitPayException BitPayException class
     * @throws BillDeliveryException BillDeliveryException class
     * @return A response status returned from the API.
     */
    public String deliverBill(String billId, String billToken) throws BitPayException {
        Map<String, String> map = new HashMap<>();
        map.put("token", billToken);
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
            HttpResponse response = this.post("bills/" + billId + "/deliveries", json);
            return this.responseToJsonString(response).replace("\"", "");
        } catch (BitPayException ex) {
            throw new BillDeliveryException(ex.getStatusCode(), ex.getReasonPhrase());
        }  catch (JsonProcessingException e) {
            throw new BitPayException(null, "Error - failed to serialize Bill object : " + e.getMessage());
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HttpResponse get(String uri, List<BasicNameValuePair> parameters) throws BitPayException {
        try {

            String fullURL = _baseUrl + uri;
            HttpGet get = new HttpGet(fullURL);

            if (parameters != null) {

                fullURL += "?" + URLEncodedUtils.format(parameters, "UTF-8");
                get.setURI(new URI(fullURL));
            }
            get.addHeader("x-bitpay-plugin-info", Env.BitpayPluginInfo);
            get.addHeader("x-accept-version", Env.BitpayApiVersion);
            get.addHeader("x-bitpay-api-frame", Env.BitpayApiFrame);
            get.addHeader("x-bitpay-api-frame-version", Env.BitpayApiFrameVersion);


            _log.info(get.toString());
            return _httpClient.execute(get);

        } catch (URISyntaxException | IOException e) {
            throw new BitPayException(null, "Error: GET failed\n" + e.getMessage());
        }
    }

    public HttpResponse get(String uri) throws BitPayException {
        return this.get(uri, null);
    }

    public HttpResponse post(String uri, String json) throws BitPayException {
        try {
            HttpPost post = new HttpPost(_baseUrl + uri);
            post.setEntity(new ByteArrayEntity(json.getBytes(StandardCharsets.UTF_8)));
            post.addHeader("x-accept-version", Env.BitpayApiVersion);
            post.addHeader("x-bitpay-plugin-info", Env.BitpayPluginInfo);
            post.addHeader("x-bitpay-api-frame", Env.BitpayApiFrame);
            post.addHeader("x-bitpay-api-frame-version", Env.BitpayApiFrameVersion);
            post.addHeader("Content-Type", "application/json");

            _log.info(post.toString());
            return _httpClient.execute(post);

        } catch (IOException e) {
            throw new BitPayException(null, "Error: POST failed\n" + e.getMessage());
        }
    }

    private String responseToJsonString(HttpResponse response) throws BitPayException {
        if (response == null) {
            throw new BitPayException(null, "Error: HTTP response is null");
        }

        try {
            // Get the JSON string from the response.
            HttpEntity entity = response.getEntity();

            String jsonString;

            jsonString = EntityUtils.toString(entity, "UTF-8");
            _log.info("RESPONSE: " + jsonString);
            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(jsonString);
            JsonNode node = rootNode.get("status");
            if (node != null) {
                if (node.toString().replace("\"", "").equals("error")) {
                    throw new BitPayException(rootNode.get("code").textValue(), rootNode.get("message").textValue());
                }
            }
            
            node = rootNode.get("error");
            
            if (node != null) {
                throw new BitPayException(null, "Error: " + node.asText());
            }

            node = rootNode.get("errors");

            if (node != null) {
                StringBuilder message = new StringBuilder("Multiple errors:");

                if (node.isArray()) {
                    for (final JsonNode errorNode : node) {
                        message.append("\n").append(errorNode.asText());
                    }

                    throw new BitPayException(null, message.toString());
                }
            }

            node = rootNode.get("data");

            if (node != null) {
                jsonString = node.toString();
            }

            return jsonString;

        } catch (JsonMappingException e) {
            throw new BitPayException(null, "Error - failed to parse json response to map : " + e.getMessage());
        } catch (BitPayException e) {
            throw new BitPayException(e.getStatusCode(), e.getReasonPhrase());
        } catch (Exception e) {
            throw new BitPayException(null, "Error - failed to retrieve HTTP response body : " + e.getMessage());
        }
    }

    private String getGuid() {
        int Min = 0;
        int Max = 99999999;

        return Min + (int) (Math.random() * ((Max - Min) + 1)) + "";
    }

    /**
     * Sets the logger level of reporting.
     *
     * @param loggerLevel int BitPayLogger constant (OFF, INFO, WARN, ERR, DEBUG)
     */
    public void setLoggerLevel(int loggerLevel) {
        _log = new BitPayLogger(loggerLevel);
    }
}
