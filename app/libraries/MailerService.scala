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

import play.api.libs.mailer._
import javax.inject.Inject
import models.User


class MailerService @Inject() (mailerClient: MailerClient) {

  private val fromAddress = "Reprompt Notifier <notifier@reprompt.com>"

  def notifyStudy(toAddress: String, user: User): String = {
    val email = Email(
      subject = "Reprompt Notification - Time to Study",
      from = fromAddress,
      bodyText = Some(createStudyNotificationBody(user)),
    )

    mailerClient.send(email)
  }

  def createStudyNotificationBody(user: User): String = {
    s"""${user.firstName},
       |
       |It's time to visit Reprompt and complete your pending content.
       |
       |See you soon,
       |
       |The Reprompt team.
     """
  }

}
