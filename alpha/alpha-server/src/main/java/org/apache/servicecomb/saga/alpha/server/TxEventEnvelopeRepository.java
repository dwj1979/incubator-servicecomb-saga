/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.saga.alpha.server;

import java.util.List;

import org.apache.servicecomb.saga.alpha.core.TxEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

interface TxEventEnvelopeRepository extends CrudRepository<TxEventEnvelope, Long> {
  List<TxEventEnvelope> findByEventGlobalTxId(String globalTxId);

  @Query("SELECT DISTINCT new org.apache.servicecomb.saga.alpha.core.TxEvent("
      + "t.event.serviceName, t.event.instanceId, t.event.globalTxId, t.event.localTxId, t.event.parentTxId, t.event.type, t.event.compensationMethod, t.event.payloads"
      + ") FROM TxEventEnvelope t "
      + "WHERE t.event.globalTxId = ?1 AND t.event.type = ?2")
  List<TxEvent> findByEventGlobalTxIdAndEventType(String globalTxId, String type);

  TxEventEnvelope findFirstByEventGlobalTxIdAndEventLocalTxIdAndEventType(String globalTxId, String localTxId, String type);

  @Query("SELECT DISTINCT new org.apache.servicecomb.saga.alpha.core.TxEvent("
      + "t.event.serviceName, t.event.instanceId, t.event.globalTxId, t.event.localTxId, t.event.parentTxId, t.event.type, t.event.compensationMethod, t.event.payloads"
      + ") FROM TxEventEnvelope t "
      + "WHERE t.event.globalTxId = ?1 AND t.event.type = 'TxStartedEvent' AND EXISTS ( "
      + "  FROM TxEventEnvelope t1 "
      + "  WHERE t1.event.globalTxId = ?1 "
      + "  AND t1.event.localTxId = t.event.localTxId "
      + "  AND t1.event.type = 'TxEndedEvent'"
      + ") AND NOT EXISTS ( "
      + "  FROM TxEventEnvelope t2 "
      + "  WHERE t2.event.globalTxId = ?1 "
      + "  AND t2.event.localTxId = t.event.localTxId "
      + "  AND t2.event.type = 'TxCompensatedEvent')")
  List<TxEvent> findStartedEventsWithMatchingEndedButNotCompensatedEvents(String globalTxId);
}
