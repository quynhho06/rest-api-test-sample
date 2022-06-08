package gss.workshop.testing.tests;

import gss.workshop.testing.pojo.board.BoardCreationRes;
import gss.workshop.testing.pojo.card.CardCreationRes;
import gss.workshop.testing.pojo.list.ListCreationRes;
import gss.workshop.testing.requests.RequestFactory;
import gss.workshop.testing.utils.ConvertUtils;
import gss.workshop.testing.utils.OtherUtils;
import gss.workshop.testing.utils.ValidationUtils;
import io.restassured.response.Response;
import org.testng.annotations.Test;

public class TrelloTests extends TestBase {

  @Test
  public void trelloWorkflowTest() {
    // 1. Create new board without default list
    String boardName = OtherUtils.randomBoardName();
    Response resBoardCreation = RequestFactory.createBoard(boardName, false);

    // VP. Validate status code
    ValidationUtils.validateStatusCode(resBoardCreation, 200);

    // VP. Validate a board is created: Board name and permission level
    BoardCreationRes board =
        ConvertUtils.convertRestResponseToPojo(resBoardCreation, BoardCreationRes.class);
    ValidationUtils.validateStringEqual(boardName, board.getName());
    ValidationUtils.validateStringEqual("private", board.getPrefs().getPermissionLevel());

    // -> Store board Id
    String boardId = board.getId();
    System.out.println(String.format("Board Id of the new Board: %s", boardId));

    // 2. Create a TODO list
    String todo = "TODO";
    Response resToDoList = RequestFactory.createList(boardId, todo);

    // VP. Validate status code
    ValidationUtils.validateStatusCode(resToDoList, 200);

    // VP. Validate a list is created: name of list, closed attribute
    ListCreationRes todoList = ConvertUtils.convertRestResponseToPojo(resToDoList, ListCreationRes.class);
    ValidationUtils.validateStringEqual(todo, todoList.getName());
    ValidationUtils.validateStringEqual("closed", todoList.getClosed());

    // VP. Validate the list was created inside the board: board Id
    String todoListId = todoList.getId();
    System.out.println(String.format("List TODO Id of the Board: %s", todoListId));

    // 3. Create a DONE list
    String done = "DONE";
    Response resDoneList = RequestFactory.createList(boardId, done);

    // VP. Validate status code
    ValidationUtils.validateStatusCode(resDoneList,200);

    // VP. Validate a list is created: name of list, "closed" property
    ListCreationRes doneList = ConvertUtils.convertRestResponseToPojo(resToDoList, ListCreationRes.class);
    ValidationUtils.validateStringEqual(done, doneList.getName());
    ValidationUtils.validateStringEqual("closed", doneList.getClosed());

    // VP. Validate the list was created inside the board: board Id
    String doneListId = doneList.getId();
    System.out.println(String.format("List DONE Id of the Board: %s", doneListId));

    // 4. Create a new Card in TODO list
    String taskName = OtherUtils.randomTaskName();
    Response resCardCreate = RequestFactory.createCard(taskName, todoList.getId());

    // VP. Validate status code
    ValidationUtils.validateStatusCode(resCardCreate, 200);

    // VP. Validate a card is created: task name, list id, board id
    CardCreationRes card = ConvertUtils.convertRestResponseToPojo(resCardCreate, CardCreationRes.class);
    ValidationUtils.validateStringEqual(taskName, card.getName());
    ValidationUtils.validateStringEqual(todoListId, card.getIdList());
    ValidationUtils.validateStringEqual(boardId, card.getIdBoard());

    // VP. Validate the card should have no votes or attachments
    ValidationUtils.validateStringEqual("", card.getIdMembersVoted());
    ValidationUtils.validateStringEqual("", card.getAttachments());

    // 5. Move the card to DONE list
    Response resDoneMove = RequestFactory.updateCard(boardId, doneListId);

    // VP. Validate status code
    ValidationUtils.validateStatusCode(resDoneMove, 200);

  }
}
