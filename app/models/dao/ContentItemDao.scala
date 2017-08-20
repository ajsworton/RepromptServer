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

import models.dto.{ AnswerDto, ContentItemDto, QuestionDto }

import scala.concurrent.{ ExecutionContext, Future }

trait ContentItemDao extends Dao[ContentItemDto] {

  /**
   * locate an item by Id.
   * @param itemId the id to match on
   * @return a future cohort
   */
  override def find(itemId: Int): Future[Option[ContentItemDto]]

  /**
   * locate a question by Id.
   * @param questionId the questionId to match on
   * @return a future question
   */
  def findQuestion(questionId: Int): Future[Option[QuestionDto]]

  /**
   *
   * @param answerId
   * @return
   */
  def findAnswer(answerId: Int): Future[Option[AnswerDto]]

  /**
   * Save an item.
   * @param itemDto the item to save
   * @return a future item
   */
  override def save(itemDto: ContentItemDto): Future[Option[ContentItemDto]]

  /**
   * Update an existing cohort
   * @param itemDto the cohort data to update (match by cohort Id)
   * @return
   */
  override def update(itemDto: ContentItemDto): Future[Option[ContentItemDto]]

  /**
   * Delete an item.
   * @param itemId the item id to delete
   * @return a future number of affected rows
   */
  def delete(itemId: Int): Future[Int]

  /**
   * Save a question.
   * @param questionDto
   * @return a future, optional question
   */
  def saveQuestion(questionDto: QuestionDto): Future[Option[QuestionDto]]

  /**
   * Save an answer.
   * @param answerDto
   * @return a future, optional answer
   */
  def saveAnswer(answerDto: AnswerDto): Future[Option[AnswerDto]]

  /**
   * update a question.
   * @param questionDto
   * @return a future, optional question
   */
  def updateQuestion(questionDto: QuestionDto): Future[Option[QuestionDto]]

  /**
   * Update an answer.
   * @param answerDto
   * @return a future, optional question
   */
  def updateAnswer(answerDto: AnswerDto): Future[Option[AnswerDto]]

  /**
   * delete a question.
   * @param questionId
   * @return a future int describing the number of affected rows
   */
  def deleteQuestion(questionId: Int): Future[Int]

  /**
   * delete an answer
   * @param answerId
   * @return a future int describing the number of affected rows
   */
  def deleteAnswer(answerId: Int): Future[Int]
}
