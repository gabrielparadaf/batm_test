/*************************************************************************************
 * Copyright (C) 2014-2020 GENERAL BYTES s.r.o. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * GENERAL BYTES s.r.o.
 * Web      :  http://www.generalbytes.com
 *
 ************************************************************************************/
package com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy;

import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.Coin;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.DepositAddress;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.Market;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.OrderResponse;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.OrderRequest;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.QuoteRequest;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.QuoteResponse;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.Wallet;
import com.generalbytes.batm.server.extensions.extra.bitcoin.exchanges.bitbuy.dto.WithdrawResult;
import com.generalbytes.batm.server.extensions.util.net.RateLimitingInterceptor;
import org.knowm.xchange.utils.nonce.CurrentTimeIncrementalNonceFactory;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.Interceptor;
import si.mazi.rescu.RestProxyFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface IBitbuyAPI {

    static IBitbuyAPI create(String apiKey, String apiSecret) throws GeneralSecurityException {
        final ClientConfig config = new ClientConfig();
        config.addDefaultParam(QueryParam.class, "apikey", apiKey);
        config.addDefaultParam(QueryParam.class, "stamp", new CurrentTimeIncrementalNonceFactory(TimeUnit.MILLISECONDS));
        config.addDefaultParam(HeaderParam.class, "signature", new BitbuyDigest(apiSecret));
        Interceptor interceptor = new RateLimitingInterceptor(IBitbuyAPI.class, 25, 30_000);
        return RestProxyFactory.createProxy(IBitbuyAPI.class, "https://partner.bcm.exchange", config, interceptor);
    }


    @GET
    @Path("/wallets")
    List<Wallet> getWallets() throws IOException;

    @GET
    @Path("/coins")
    List<Coin> getCoins() throws IOException;

    @GET
    @Path("/markets")
    List<Market> getMarkets() throws IOException;

    /**
     * @return deposit address for the given coin (new address is NOT generated on each call)
     */
    @GET
    @Path("/wallets/{coin}/deposit-address")
    DepositAddress getDepositAddress(@PathParam("coin") String coin) throws IOException;

    @POST
    @Path("/wallets/{coin}/withdraw")
    WithdrawResult withdraw(@PathParam("coin") String coin, @QueryParam("address") String address, @QueryParam("amount") BigDecimal amount) throws IOException;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/submit/order")
    OrderResponse submitOrder(OrderRequest orderRequest) throws IOException;

    @GET
    @Path("/single-order")
    OrderResponse getOrder(@QueryParam("marketSymbol") String marketSymbol, @QueryParam("orderId") String orderId) throws IOException;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/quote/trade")
    QuoteResponse quoteOrder(QuoteRequest quoteRequest) throws IOException;

}
