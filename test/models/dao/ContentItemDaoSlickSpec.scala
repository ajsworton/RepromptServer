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

package models.dao

import libs.{ AppFactory, TestingDbQueries }
import models.dto.{ AnswerDto, ContentItemDto, QuestionDto, ScoreDto }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }

class ContentItemDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter with AppFactory {

  val service: ContentItemDaoSlick = fakeApplication().injector.instanceOf[ContentItemDaoSlick]
  val database: TestingDbQueries = fakeApplication().injector.instanceOf[TestingDbQueries]
  val teacherId, packageId = 8744
  val studentId = 8745
  val otherStudentId = 8746

  val fakeQuestion = QuestionDto(None, "Question", "MCSA", teacherId, None)
  val fakeAnswer = AnswerDto(None, Some(teacherId), "The Answer goes here", correct = true)

  val fakeContentItem = ContentItemDto(None, packageId, None, "Name", "Content", enabled = true, None, None)

  before {
    database.insertStudyContent(teacherId, studentId, otherStudentId)
  }

  after {
    database.clearStudyContent(teacherId, studentId, otherStudentId)
  }

  describe("find(itemId: Int)") {
    it("should find an item by id") {
      for {
        found <- service.find(teacherId)
        assertion = {
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.id.get should be(teacherId)
        }
      } yield assertion
    }
  }

  describe("findQuestion(questionId: Int)") {
    it("should find question by supplied id") {
      for {
        found <- service.findQuestion(teacherId)
        assertion = {
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.id.get should be(teacherId)
        }
      } yield assertion
    }
  }

  describe("findAnswer(answerId: Int)") {
    it("should find an answer by id") {
      for {
        found <- service.findAnswer(teacherId)
        assertion = {
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.id.get should be(teacherId)
        }
      } yield assertion
    }
  }

  describe("save(itemDto: ContentItemDto)") {
    it("should save a supplied item") {
      for {
        saved <- service.save(fakeContentItem)
        found <- service.find(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.name should be("Name")
          found.get.content should be("Content")
        }
      } yield assertion
    }
  }

  describe("saveQuestion(questionDto: QuestionDto)") {
    it("should save a supplied question") {
      for {
        saved <- service.saveQuestion(fakeQuestion)
        found <- service.findQuestion(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.question should be(fakeQuestion.question)
          found.get.format should be(fakeQuestion.format)
        }
      } yield assertion
    }
  }

  describe("saveAnswer(answerDto: AnswerDto)") {
    it("should save a supplied answer") {
      for {
        saved <- service.saveAnswer(fakeAnswer)
        found <- service.findAnswer(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.answer should be(fakeAnswer.answer)
          found.get.correct should be(fakeAnswer.correct)
        }
      } yield assertion
    }
  }

  describe("updateQuestion(questionDto: QuestionDto)") {
    it("should update a supplied question") {
      val newQuestion = "new question"
      for {
        saved <- service.saveQuestion(fakeQuestion)
        found <- service.findQuestion(saved.get.id.get)
        _ <- service.updateQuestion(found.get.copy(question = newQuestion))
        updated <- service.findQuestion(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.question should be(fakeQuestion.question)
          found.get.format should be(fakeQuestion.format)
          updated.get.question should be(newQuestion)
        }
      } yield assertion
    }
  }

  describe("updateAnswer(answerDto: AnswerDto)") {
    it("should update a supplied answer") {
      val newAnswer = "new answer"
      for {
        saved <- service.saveAnswer(fakeAnswer)
        found <- service.findAnswer(saved.get.id.get)
        _ <- service.updateAnswer(found.get.copy(answer = newAnswer))
        updated <- service.findAnswer(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          found.isDefined should be(true)
          found.get.id.isDefined should be(true)
          found.get.answer should be(fakeAnswer.answer)
          found.get.correct should be(fakeAnswer.correct)
          updated.get.answer should be(newAnswer)
        }
      } yield assertion
    }
  }

  describe("deleteQuestion(questionId: Int)") {
    it("should delete a question by supplied id") {
      for {
        saved <- service.saveQuestion(fakeQuestion)
        before <- service.findQuestion(saved.get.id.get)
        deleted <- service.deleteQuestion(before.get.id.get)
        after <- service.findQuestion(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          before.isDefined should be(true)
          deleted should be(1)
          after should be(None)
        }
      } yield assertion
    }
  }

  describe("deleteAnswer(answerId: Int)") {
    it("should delete an answer byh supplied id") {
      for {
        saved <- service.saveAnswer(fakeAnswer)
        before <- service.findAnswer(saved.get.id.get)
        deleted <- service.deleteAnswer(before.get.id.get)
        after <- service.findAnswer(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          before.isDefined should be(true)
          deleted should be(1)
          after should be(None)
        }
      } yield assertion
    }
  }

  describe("update(itemDto: ContentItemDto)") {
    it("should update a supplied item") {
      val newName = "FishName"
      for {
        saved <- service.save(fakeContentItem)
        before <- service.find(saved.get.id.get)
        _ <- service.update(before.get.copy(name = newName))
        after <- service.find(saved.get.id.get)
        assertion = {
          saved.isDefined should be(true)
          before.isDefined should be(true)
          after.isDefined should be(true)
          before.get.id.isDefined should be(true)
          after.get.id.isDefined should be(true)
          after.get.id should be(before.get.id)
          before.get.name should be("Name")
          after.get.name should be(newName)
        }
      } yield assertion
    }
  }

  describe("delete(itemId: Int)") {
    it("should delete an item by supplied id") {
      for {
        saved <- service.save(fakeContentItem)
        before <- service.find(saved.get.id.get)
        _ <- service.delete(saved.get.id.get)
        after <- service.find(saved.get.id.get)

        assertion = {
          saved.isDefined should be(true)
          before.isDefined should be(true)
          after should be(None)
        }
      } yield assertion
    }
  }

}
