// Copyright (C) 2017 Alexander Worton.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package libraries

import libs.AppFactory
import models.User
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any
import org.scalatest.{FunSpec, Matchers}
import play.api.libs.mailer.{Email, MailerClient}



class MailerServiceSpec extends FunSpec with Matchers with MockitoSugar with AppFactory {

  //val mailerService: MailerService = fakeApplication().injector.instanceOf[MailerService]
  val mockMailerClient = mock[MailerClient]
  when(mockMailerClient.send(any[Email])).thenReturn("msgId")
  val mailerService = new MailerService(mockMailerClient)
  val user = User(firstName= "Andy", surName = "Baloo", email = "a.baloo@reprompt.com")
  var data = Email(
    subject = "Reprompt Notification - Time to Study",
    from = getUserAddress(user),
    bodyText = Some(mailerService.createStudyNotificationBody(user)),
  )

  describe("notifyStudy") {
    it("should create a notification to the provided user and return the message id") {

      val response = mailerService.notifyStudy(getUserAddress(user), user)
      response should be ("msgId")
    }
  }

  private def getUserAddress(user: User): String = {
    s"${user.firstName} ${user.surName} <${user.email}>"
  }

}
