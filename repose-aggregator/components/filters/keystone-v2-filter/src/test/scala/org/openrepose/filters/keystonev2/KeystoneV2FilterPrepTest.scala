/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.filters.keystonev2

import java.net.URL

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.test.appender.ListAppender
import org.junit.runner.RunWith
import org.mockito.AdditionalMatchers._
import org.mockito.Matchers._
import org.mockito.{ArgumentCaptor, Mockito, Matchers => MockitoMatcher}
import org.openrepose.core.services.config.ConfigurationService
import org.openrepose.core.services.datastore.{Datastore, DatastoreService}
import org.openrepose.core.services.serviceclient.akka.{AkkaServiceClient, AkkaServiceClientFactory}
import org.openrepose.core.systemmodel.config.{SystemModel, TracingHeaderConfig}
import org.openrepose.filters.keystonev2.config.KeystoneV2AuthenticationConfig
import org.openrepose.nodeservice.atomfeed.{AtomFeedListener, AtomFeedService}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}
import org.springframework.mock.web.MockFilterConfig

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class KeystoneV2FilterPrepTest extends FunSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockDatastoreService = mock[DatastoreService]
  private val mockDatastore = mock[Datastore]
  Mockito.when(mockDatastoreService.getDefaultDatastore).thenReturn(mockDatastore)
  private val mockSystemModel = mock[SystemModel]
  private val mockTracingHeader = mock[TracingHeaderConfig]
  Mockito.when(mockSystemModel.getTracingHeader).thenReturn(mockTracingHeader)
  Mockito.when(mockTracingHeader.isEnabled).thenReturn(true, Nil: _*)

  override def beforeEach(): Unit = {
    Mockito.reset(mockDatastore)
  }

  describe("when the filter is initialized") {
    it("should initialize the configuration") {
      val mockAkkaServiceClient = mock[AkkaServiceClient]
      val mockAkkaServiceClientFactory = mock[AkkaServiceClientFactory]
      val mockConfigurationService = mock[ConfigurationService]
      Mockito.when(mockAkkaServiceClientFactory.newAkkaServiceClient(or(anyString(), isNull.asInstanceOf[String]))).thenReturn(mockAkkaServiceClient)
      val filter = new KeystoneV2Filter(mockConfigurationService, mockAkkaServiceClientFactory, mock[AtomFeedService], mockDatastoreService)

      val config = new MockFilterConfig("KeystoneV2Filter")

      filter.init(config)

      val resourceCaptor = ArgumentCaptor.forClass(classOf[URL])
      Mockito.verify(mockConfigurationService).subscribeTo(
        MockitoMatcher.eq("KeystoneV2Filter"),
        MockitoMatcher.eq("keystone-v2.cfg.xml"),
        resourceCaptor.capture,
        MockitoMatcher.eq(filter),
        MockitoMatcher.eq(classOf[KeystoneV2AuthenticationConfig]))
      Mockito.verify(mockConfigurationService).subscribeTo(
        MockitoMatcher.eq("system-model.cfg.xml"),
        MockitoMatcher.any(classOf[URL]),
        MockitoMatcher.eq(filter.SystemModelConfigListener),
        MockitoMatcher.eq(classOf[SystemModel]))

      assert(resourceCaptor.getValue.toString.endsWith("/META-INF/schema/config/keystone-v2.xsd"))
    }

    it("should initialize a configuration with a different name") {
      val mockAkkaServiceClient = mock[AkkaServiceClient]
      val mockAkkaServiceClientFactory = mock[AkkaServiceClientFactory]
      val mockConfigurationService = mock[ConfigurationService]
      Mockito.when(mockAkkaServiceClientFactory.newAkkaServiceClient(or(anyString(), isNull.asInstanceOf[String]))).thenReturn(mockAkkaServiceClient)
      val filter = new KeystoneV2Filter(mockConfigurationService, mockAkkaServiceClientFactory, mock[AtomFeedService], mockDatastoreService)

      val config = new MockFilterConfig("KeystoneV2Filter")
      config.addInitParameter("filter-config", "some-other-config.xml")

      filter.init(config)

      val resourceCaptor = ArgumentCaptor.forClass(classOf[URL])
      Mockito.verify(mockConfigurationService).subscribeTo(
        MockitoMatcher.eq("KeystoneV2Filter"),
        MockitoMatcher.eq("some-other-config.xml"),
        resourceCaptor.capture,
        MockitoMatcher.eq(filter),
        MockitoMatcher.eq(classOf[KeystoneV2AuthenticationConfig]))
      Mockito.verify(mockConfigurationService).subscribeTo(
        MockitoMatcher.eq("system-model.cfg.xml"),
        MockitoMatcher.any(classOf[URL]),
        MockitoMatcher.eq(filter.SystemModelConfigListener),
        MockitoMatcher.eq(classOf[SystemModel]))

      assert(resourceCaptor.getValue.toString.endsWith("/META-INF/schema/config/keystone-v2.xsd"))
    }
  }

  it("deregisters from the configuration service when destroying") {
    val mockConfigurationService = mock[ConfigurationService]
    val filter = new KeystoneV2Filter(mockConfigurationService, mock[AkkaServiceClientFactory], mock[AtomFeedService], mockDatastoreService)

    val config = new MockFilterConfig
    filter.init(config)
    filter.destroy()

    Mockito.verify(mockConfigurationService).unsubscribeFrom("keystone-v2.cfg.xml", filter)
  }

  describe("when the configuration is updated") {
    it("sets the current configuration on the filter asserting the defaults and initialized is true") {
      val filter = new KeystoneV2Filter(mock[ConfigurationService], mock[AkkaServiceClientFactory], mock[AtomFeedService], mockDatastoreService)
      filter.isInitialized shouldBe false

      val configuration = Marshaller.keystoneV2ConfigFromString(
        """<?xml version="1.0" encoding="UTF-8"?>
          |
          |<keystone-v2 xmlns="http://docs.openrepose.org/repose/keystone-v2/v1.0">
          |<identity-service
          |  username="user"
          |  password="pass"
          |  uri="https://lol.com"
          |  />
          |</keystone-v2>
        """.stripMargin)

      filter.configurationUpdated(configuration)
      filter.SystemModelConfigListener.configurationUpdated(mockSystemModel)

      val config = filter.configuration
      val timeouts = config.getCache.getTimeouts
      timeouts.getEndpoints shouldBe 600
      timeouts.getGroup shouldBe 600
      timeouts.getToken shouldBe 600
      timeouts.getVariability shouldBe 0

      config.getIdentityService.isSetGroupsInHeader shouldBe true
      config.getIdentityService.isSetCatalogInHeader shouldBe false

      config.getDelegating shouldBe null

      config.getWhiteList.getUriRegex.size() shouldBe 0

      config.getTenantHandling.getValidateTenant shouldBe null

      config.getRequireServiceEndpoint shouldBe null
    }

    it("sets the default delegating quality to 0.7") {
      val filter = new KeystoneV2Filter(mock[ConfigurationService], mock[AkkaServiceClientFactory], mock[AtomFeedService], mockDatastoreService)
      filter.isInitialized shouldBe false

      val configuration = Marshaller.keystoneV2ConfigFromString(
        """<?xml version="1.0" encoding="UTF-8"?>
          |
          |<keystone-v2 xmlns="http://docs.openrepose.org/repose/keystone-v2/v1.0">
          |<delegating/>
          |<identity-service
          |  username="user"
          |  password="pass"
          |  uri="https://lol.com"
          |  />
          |</keystone-v2>
        """.stripMargin)

      filter.configurationUpdated(configuration)
      filter.SystemModelConfigListener.configurationUpdated(mockSystemModel)

      filter.configuration.getDelegating.getQuality shouldBe 0.7
    }

    it("should register to listen to the Atom Feed") {
      val mockAtomFeedService = mock[AtomFeedService]
      val filter = new KeystoneV2Filter(mock[ConfigurationService], mock[AkkaServiceClientFactory], mockAtomFeedService, mockDatastoreService)

      filter.configurationUpdated(Marshaller.keystoneV2ConfigFromString(
        """<?xml version="1.0" encoding="UTF-8"?>
          |
          |<keystone-v2 xmlns="http://docs.openrepose.org/repose/keystone-v2/v1.0">
          |    <delegating/>
          |    <identity-service uri="https://lol.com"/>
          |    <cache>
          |        <atom-feed id="some-feed"/>
          |    </cache>
          |</keystone-v2>
        """.stripMargin
      ))

      Mockito.verify(mockAtomFeedService).registerListener(MockitoMatcher.eq("some-feed"), MockitoMatcher.any[AtomFeedListener])
    }


    it("should unregister the old feeds and register the new ones") {
      val mockAtomFeedService = mock[AtomFeedService]
      val rickFeed = "rick-feed"
      val mortyFeed = "morty-feed"
      val rickId = rickFeed + "-ID"
      val mortyId = mortyFeed + "-ID"
      Mockito.when(
        mockAtomFeedService.registerListener(MockitoMatcher.eq(rickFeed), MockitoMatcher.any[AtomFeedListener])
      ).thenReturn(rickId)
      Mockito.when(
        mockAtomFeedService.registerListener(MockitoMatcher.eq(mortyFeed), MockitoMatcher.any[AtomFeedListener])
      ).thenReturn(mortyId)
      val filter = new KeystoneV2Filter(mock[ConfigurationService], mock[AkkaServiceClientFactory], mockAtomFeedService, mockDatastoreService)

      filter.configurationUpdated(Marshaller.keystoneV2ConfigFromString(
        s"""<?xml version="1.0" encoding="UTF-8"?>
          |
          |<keystone-v2 xmlns="http://docs.openrepose.org/repose/keystone-v2/v1.0">
          |    <delegating/>
          |    <identity-service uri="https://lol.com"/>
          |    <cache>
          |        <atom-feed id="$rickFeed"/>
          |        <atom-feed id="$mortyFeed"/>
          |    </cache>
          |</keystone-v2>
        """.stripMargin
      ))

      Mockito.verify(mockAtomFeedService).registerListener(MockitoMatcher.eq(rickFeed), MockitoMatcher.any[AtomFeedListener])
      Mockito.verify(mockAtomFeedService).registerListener(MockitoMatcher.eq(mortyFeed), MockitoMatcher.any[AtomFeedListener])

      filter.configurationUpdated(Marshaller.keystoneV2ConfigFromString(
        """<?xml version="1.0" encoding="UTF-8"?>
          |
          |<keystone-v2 xmlns="http://docs.openrepose.org/repose/keystone-v2/v1.0">
          |    <delegating/>
          |    <identity-service uri="https://lol.com"/>
          |    <cache>
          |        <atom-feed id="some-feed"/>
          |    </cache>
          |</keystone-v2>
        """.stripMargin
      ))

      Mockito.verify(mockAtomFeedService).unregisterListener(rickId)
      Mockito.verify(mockAtomFeedService).unregisterListener(mortyId)

      Mockito.verify(mockAtomFeedService).registerListener(MockitoMatcher.eq("some-feed"), MockitoMatcher.any[AtomFeedListener])
    }
  }

  it("should fail to unmarshal duplicate Atom Feed ID's") {
    val ctx = LogManager.getContext(false).asInstanceOf[LoggerContext]
    val listAppender = ctx.getConfiguration.getAppender("List0").asInstanceOf[ListAppender]
    listAppender.clear()
    intercept[ClassCastException] {
      Marshaller.keystoneV2ConfigFromString(
        """<?xml version="1.0" encoding="UTF-8"?>
          |
          |<keystone-v2 xmlns="http://docs.openrepose.org/repose/keystone-v2/v1.0">
          |    <delegating/>
          |    <identity-service uri="https://lol.com"/>
          |    <cache>
          |        <atom-feed id="duplicate-feed"/>
          |        <atom-feed id="duplicate-feed"/>
          |    </cache>
          |</keystone-v2>
        """.stripMargin
      )
    }
    listAppender.getEvents.exists(_.getMessage.getFormattedMessage.contains("Atom Feed ID's must be unique")) shouldBe true
  }
}