package com.bitpay.sdk_light;

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
 * @author Antonio Buedo
 * @version 1.0.1909
 * See bitpay.com/api for more information.
 * date 01.09.2019
 */

public class Client {

    private static final BitPayLogger _log = new BitPayLogger(BitPayLogger.DEBUG);
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
            throw new BitPayException("Error - failed to deserialize BitPay server response (Config) : " + e.getMessage());
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
            throw new BitPayException("Error - failed to deserialize BitPay server response (Config) : " + e.getMessage());
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
            throw new BitPayException("Error - failed to deserialize BitPay server response (Token array) : " + e.getMessage());
        }
    }

    /**
     * Create a BitPay invoice using the Merchant facade.
     *
     * @param invoice An Invoice object with request parameters defined.
     * @return A BitPay generated Invoice object.
     * @throws BitPayException BitPayException class
     */
    public Invoice createInvoice(Invoice invoice) throws BitPayException {
        invoice.setToken(this._token);
        invoice.setGuid(this.getGuid());

        ObjectMapper mapper = new ObjectMapper();

        String json;

        try {
            json = mapper.writeValueAsString(invoice);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to serialize Invoice object : " + e.getMessage());
        }

        HttpResponse response = this.post("invoices", json);

        try {
            invoice = mapper.readerForUpdating(invoice).readValue(this.responseToJsonString(response));
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }

        return invoice;
    }

    /**
     * Retrieve a BitPay invoice by invoice id using the public facade.
     *
     * @param invoiceId The id of the invoice to retrieve.
     * @return A BitPay Invoice object.
     * @throws BitPayException BitPayException class
     */
    public Invoice getInvoice(String invoiceId) throws BitPayException {
        final List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", this._token));

        HttpResponse response = this.get("invoices/" + invoiceId, params);

        Invoice invoice;
        try {
            invoice = new ObjectMapper().readValue(this.responseToJsonString(response), Invoice.class);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Invoice) : " + e.getMessage());
        }

        return invoice;
    }

    /**
     * Retrieve the exchange rate table maintained by BitPay.  See https://bitpay.com/bitcoin-exchange-rates.
     *
     * @return A Rates object populated with the BitPay exchange rate table.
     * @throws BitPayException BitPayException class
     */
    public Rates getRates() throws BitPayException {
        HttpResponse response = this.get("rates");

        List<Rate> rates;

        try {
            rates = Arrays.asList(new ObjectMapper().readValue(this.responseToJsonString(response), Rate[].class));
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Rates) : " + e.getMessage());
        }

        return new Rates(rates, this);
    }

    /**
     * Create a BitPay bill using the POS facade.
     *
     * @param bill An Bill object with request parameters defined.
     * @return A BitPay generated Bill object.
     * @throws BitPayException BitPayException class
     */
    public Bill createBill(Bill bill) throws BitPayException {
        String token = this._token;
        bill.setToken(token);
        ObjectMapper mapper = new ObjectMapper();
        String json;

        try {
            json = mapper.writeValueAsString(bill);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to serialize Bill object : " + e.getMessage());
        }

        HttpResponse response = this.post("bills", json);

        try {
            bill = mapper.readerForUpdating(bill).readValue(this.responseToJsonString(response));
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Bill) : " + e.getMessage());
        }

        return bill;
    }

    /**
     * Retrieve a BitPay bill by bill id using the public facade.
     *
     * @param billId The id of the bill to retrieve.
     * @return A BitPay Bill object.
     * @throws BitPayException BitPayException class
     */
    public Bill getBill(String billId) throws BitPayException {
        String token = this._token;
        final List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", token));

        HttpResponse response = this.get("bills/" + billId, params);

        Bill bill;
        try {
            bill = new ObjectMapper().readValue(this.responseToJsonString(response), Bill.class);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to deserialize BitPay server response (Bill) : " + e.getMessage());
        }

        return bill;
    }

    /**
     * Deliver a BitPay Bill.
     *
     * @param billId    The id of the requested bill.
     * @param billToken The token of the requested bill.
     * @throws BitPayException BitPayException class
     * @return A response status returned from the API.
     */
    public String deliverBill(String billId, String billToken) throws BitPayException {
        Map<String, String> map = new HashMap<>();
        map.put("token", billToken);
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new BitPayException("Error - failed to serialize Bill object : " + e.getMessage());
        }
        HttpResponse response = this.post("bills/" + billId + "/deliveries", json);

        return this.responseToJsonString(response).replace("\"", "");
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


            _log.info(get.toString());
            return _httpClient.execute(get);

        } catch (URISyntaxException | IOException e) {
            throw new BitPayException("Error: GET failed\n" + e.getMessage());
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
            post.addHeader("Content-Type", "application/json");

            _log.info(post.toString());
            return _httpClient.execute(post);

        } catch (IOException e) {
            throw new BitPayException("Error: POST failed\n" + e.getMessage());
        }
    }

    private String responseToJsonString(HttpResponse response) throws BitPayException {
        if (response == null) {
            throw new BitPayException("Error: HTTP response is null");
        }

        try {
            // Get the JSON string from the response.
            HttpEntity entity = response.getEntity();

            String jsonString;

            jsonString = EntityUtils.toString(entity, "UTF-8");
            _log.info("RESPONSE: " + jsonString);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(jsonString);
            JsonNode node = rootNode.get("error");

            if (node != null) {
                throw new BitPayException("Error: " + node.asText());
            }

            node = rootNode.get("errors");

            if (node != null) {
                StringBuilder message = new StringBuilder("Multiple errors:");

                if (node.isArray()) {
                    for (final JsonNode errorNode : node) {
                        message.append("\n").append(errorNode.asText());
                    }

                    throw new BitPayException(message.toString());
                }
            }

            node = rootNode.get("data");

            if (node != null) {
                jsonString = node.toString();
            }

            return jsonString;

        } catch (JsonMappingException e) {
            throw new BitPayException("Error - failed to parse json response to map : " + e.getMessage());
        } catch (Exception e) {
            throw new BitPayException("Error - failed to retrieve HTTP response body : " + e.getMessage());
        }
    }

    private String getGuid() {
        int Min = 0;
        int Max = 99999999;

        return Min + (int) (Math.random() * ((Max - Min) + 1)) + "";
    }
}
