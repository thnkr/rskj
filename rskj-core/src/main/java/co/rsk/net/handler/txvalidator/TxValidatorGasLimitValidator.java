/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.net.handler.txvalidator;

import org.ethereum.core.AccountState;
import org.ethereum.core.Transaction;

import java.math.BigInteger;

/**
 * Checks that the transaction gas limit is lower than the `block` gas limit,
 * though there's no check that the actual block gas limit is used
 */
public class TxValidatorGasLimitValidator implements TxValidatorStep {

    @Override
    public boolean validate(Transaction tx, AccountState state, byte[] gasLimit, byte[] minimumGasPrice, long bestBlockNumber) {
        BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());
        return  txGasLimit.compareTo(new BigInteger(1, gasLimit)) <= 0;
    }
}
