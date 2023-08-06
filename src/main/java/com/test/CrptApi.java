package com.test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private volatile Integer requestLimit;
    private volatile AtomicInteger requestCounter = new AtomicInteger(0);
    private ObjectMapper objectMapper;
    private HttpClient client;

    public CrptApi(TimeUnit timeUnit, Integer requestLimit, HttpClient client, ObjectMapper objectMapper, Timer timer) {
        this.requestLimit = requestLimit;
        this.client = client;
        this.objectMapper = objectMapper;
        timer.scheduleAtFixedRate(resetRequestCounter(), 0, timeUnit.toMillis(1));
    }

    public void createDocumentToSendInSalesProductMadeInRussia(ProductMadeInRussiaToSendInSalesDocument productMadeInRussiaToSendInSalesDocument, String signToken) {
        try {
            String body = objectMapper.writeValueAsString(productMadeInRussiaToSendInSalesDocument);
            sendRequestAndGetResponse(makeSignedRequest(body, signToken));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest makeSignedRequest(String body, String signToken) {
        return HttpRequest
                .newBuilder(URI.create(ApiUriDictionary.PUSH_TO_SALES_PRODUCT_MADE_IN_RUSSIA_URI))
                .header("Content-type", "Application/json")
                .header("Authorization", "Bearer " + signToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpResponse sendRequestAndGetResponse(HttpRequest request) {
        try {
            return checkLimitationAndSendRequest(request).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<HttpResponse> checkLimitationAndSendRequest(HttpRequest request) {
        while (true) {
            synchronized (requestCounter) {
                if (requestCounter.get() < requestLimit) {
                    requestCounter.incrementAndGet();
                    return sendRequest(request);
                }
            }
        }
    }

    private CompletableFuture<HttpResponse> sendRequest(HttpRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.err.println("Sent");
                return client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private TimerTask resetRequestCounter() {
        return new TimerTask() {
            public void run() {
                requestCounter.set(0);
            }
        };
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ProductMadeInRussiaToSendInSalesDocument {

        @JsonProperty("description")
        private Description description;//not required

        @JsonProperty("doc_id")
        private String documentId;

        @JsonProperty("doc_status")
        private String documentStatus;

        @JsonProperty("doc_type")
        private String documentType;

        @JsonProperty("importRequest")
        private Boolean importRequest;//not required

        @JsonProperty("owner_inn")
        private String ownerInn;

        @JsonProperty("participant_inn")
        private String participantInn;

        @JsonProperty("producer_inn")
        private String producerInn;

        @JsonProperty("production_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate productionDate;

        @JsonProperty("production_type")
        private String productionType;

        @JsonProperty("products")
        private List<Product> products;//not required

        @JsonProperty("reg_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate regDate;

        @JsonProperty("reg_number")
        private String regNumber;//not required



        public ProductMadeInRussiaToSendInSalesDocument() {
        }

        public ProductMadeInRussiaToSendInSalesDocument(String documentId, String documentStatus, String documentType, String ownerInn,
                                                        String participantInn, String producerInn, LocalDate productionDate,
                                                        String productionType, LocalDate regDate) {
            this.documentId = documentId;
            this.documentStatus = documentStatus;
            this.documentType = documentType;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.regDate = regDate;
        }

        public ProductMadeInRussiaToSendInSalesDocument(Description description, String documentId, String documentStatus,
                                                        String documentType, Boolean importRequest, String ownerInn, String participantInn,
                                                        String producerInn, LocalDate productionDate, String productionType,
                                                        List<Product> products, LocalDate regDate, String regNumber) {
            this.description = description;
            this.documentId = documentId;
            this.documentStatus = documentStatus;
            this.documentType = documentType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }



        public Description getDescription() {
            return description;
        }

        public void setDescription(String participantInn) {
            this.description = new Description(participantInn);
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getDocumentStatus() {
            return documentStatus;
        }

        public void setDocumentStatus(String documentStatus) {
            this.documentStatus = documentStatus;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public Boolean getImportRequest() {
            return importRequest;
        }

        public void setImportRequest(Boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public LocalDate getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(LocalDate productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public LocalDate getRegDate() {
            return regDate;
        }

        public void setRegDate(LocalDate regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductMadeInRussiaToSendInSalesDocument that = (ProductMadeInRussiaToSendInSalesDocument) o;
            if (description != null ? !description.equals(that.description) : that.description != null) return false;
            if (documentId != null ? !documentId.equals(that.documentId) : that.documentId != null) return false;
            if (documentStatus != null ? !documentStatus.equals(that.documentStatus) : that.documentStatus != null)
                return false;
            if (documentType != null ? !documentType.equals(that.documentType) : that.documentType != null)
                return false;
            if (importRequest != null ? !importRequest.equals(that.importRequest) : that.importRequest != null)
                return false;
            if (ownerInn != null ? !ownerInn.equals(that.ownerInn) : that.ownerInn != null) return false;
            if (participantInn != null ? !participantInn.equals(that.participantInn) : that.participantInn != null)
                return false;
            if (producerInn != null ? !producerInn.equals(that.producerInn) : that.producerInn != null) return false;
            if (productionDate != null ? !productionDate.equals(that.productionDate) : that.productionDate != null)
                return false;
            if (productionType != null ? !productionType.equals(that.productionType) : that.productionType != null)
                return false;
            if (products != null ? !products.equals(that.products) : that.products != null) return false;
            if (regDate != null ? !regDate.equals(that.regDate) : that.regDate != null) return false;
            return regNumber != null ? regNumber.equals(that.regNumber) : that.regNumber == null;
        }

        @Override
        public int hashCode() {
            int result = description != null ? description.hashCode() : 0;
            result = 31 * result + (documentId != null ? documentId.hashCode() : 0);
            result = 31 * result + (documentStatus != null ? documentStatus.hashCode() : 0);
            result = 31 * result + (documentType != null ? documentType.hashCode() : 0);
            result = 31 * result + (importRequest != null ? importRequest.hashCode() : 0);
            result = 31 * result + (ownerInn != null ? ownerInn.hashCode() : 0);
            result = 31 * result + (participantInn != null ? participantInn.hashCode() : 0);
            result = 31 * result + (producerInn != null ? producerInn.hashCode() : 0);
            result = 31 * result + (productionDate != null ? productionDate.hashCode() : 0);
            result = 31 * result + (productionType != null ? productionType.hashCode() : 0);
            result = 31 * result + (products != null ? products.hashCode() : 0);
            result = 31 * result + (regDate != null ? regDate.hashCode() : 0);
            result = 31 * result + (regNumber != null ? regNumber.hashCode() : 0);
            return result;
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Description {

        @JsonProperty("participantInn")
        private String participantInn;



        public Description() {
        }

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }



        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Description that = (Description) o;

            return participantInn != null ? participantInn.equals(that.participantInn) : that.participantInn == null;
        }

        @Override
        public int hashCode() {
            return participantInn != null ? participantInn.hashCode() : 0;
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Product {

        @JsonProperty("certificate_document")
        private String certificateDocument;//not required

        @JsonProperty("certificate_document_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate certificateDocumentDate;//not required

        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;//not required

        @JsonProperty("owner_inn")
        private String ownerInn;

        @JsonProperty("producer_inn")
        private String producerInn;

        @JsonProperty("production_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate productionDate;

        @JsonProperty("tnved_code")
        private String tnvedCode;

        @JsonProperty("uit_code")
        private String uitCode;//not required

        @JsonProperty("uitu_code")
        private String uituCode;//not required



        public Product() {
        }

        public Product(String ownerInn, String producerInn, LocalDate productionDate, String tnvedCode) {
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
        }

        public Product(String certificateDocument, LocalDate certificateDocumentDate, String certificateDocumentNumber,
                       String ownerInn, String producerInn, LocalDate productionDate, String tnvedCode, String uitCode,
                       String uituCode) {
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
            this.uitCode = uitCode;
            this.uituCode = uituCode;
        }



        public String getCertificateDocument() {
            return certificateDocument;
        }

        public void setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
        }

        public LocalDate getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public void setCertificateDocumentDate(LocalDate certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public void setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public LocalDate getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(LocalDate productionDate) {
            this.productionDate = productionDate;
        }

        public String getTnvedCode() {
            return tnvedCode;
        }

        public void setTnvedCode(String tnvedCode) {
            this.tnvedCode = tnvedCode;
        }

        public String getUitCode() {
            return uitCode;
        }

        public void setUitCode(String uitCode) {
            this.uitCode = uitCode;
        }

        public String getUituCode() {
            return uituCode;
        }

        public void setUituCode(String uituCode) {
            this.uituCode = uituCode;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Product product = (Product) o;
            if (certificateDocument != null ? !certificateDocument.equals(product.certificateDocument) : product.certificateDocument != null)
                return false;
            if (certificateDocumentDate != null ? !certificateDocumentDate.equals(product.certificateDocumentDate) : product.certificateDocumentDate != null)
                return false;
            if (certificateDocumentNumber != null ? !certificateDocumentNumber.equals(product.certificateDocumentNumber) : product.certificateDocumentNumber != null)
                return false;
            if (ownerInn != null ? !ownerInn.equals(product.ownerInn) : product.ownerInn != null) return false;
            if (producerInn != null ? !producerInn.equals(product.producerInn) : product.producerInn != null)
                return false;
            if (productionDate != null ? !productionDate.equals(product.productionDate) : product.productionDate != null)
                return false;
            if (tnvedCode != null ? !tnvedCode.equals(product.tnvedCode) : product.tnvedCode != null) return false;
            if (uitCode != null ? !uitCode.equals(product.uitCode) : product.uitCode != null) return false;
            return uituCode != null ? uituCode.equals(product.uituCode) : product.uituCode == null;
        }

        @Override
        public int hashCode() {
            int result = certificateDocument != null ? certificateDocument.hashCode() : 0;
            result = 31 * result + (certificateDocumentDate != null ? certificateDocumentDate.hashCode() : 0);
            result = 31 * result + (certificateDocumentNumber != null ? certificateDocumentNumber.hashCode() : 0);
            result = 31 * result + (ownerInn != null ? ownerInn.hashCode() : 0);
            result = 31 * result + (producerInn != null ? producerInn.hashCode() : 0);
            result = 31 * result + (productionDate != null ? productionDate.hashCode() : 0);
            result = 31 * result + (tnvedCode != null ? tnvedCode.hashCode() : 0);
            result = 31 * result + (uitCode != null ? uitCode.hashCode() : 0);
            result = 31 * result + (uituCode != null ? uituCode.hashCode() : 0);
            return result;
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ApiUriDictionary {
        private static final String PUSH_TO_SALES_PRODUCT_MADE_IN_RUSSIA_URI = "https://postman-echo.com/post";
    }
}
