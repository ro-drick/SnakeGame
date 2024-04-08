package application;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
	private static final int CELL_SIZE = 10;
	private static final int GRID_SIZE = 60;
	private static final int SPEED = 10;

	private Rectangle snakeHead = new Rectangle(CELL_SIZE, CELL_SIZE, Color.WHITE);
	private ArrayList<Rectangle> tailSegment = new ArrayList<>();
	private GridPane grid = new GridPane();
	private Circle snakeFood = new Circle();
	private int currentCol = 4;
	private int currentRow = 5;
	private Direction direction = Direction.RIGHT;

	Random random = new Random();
	int randomX = random.nextInt(GRID_SIZE);
	int randomY = random.nextInt(GRID_SIZE);
	boolean collisionDetected = false;

	private AnimationTimer timer;
	private Timeline timeline;
	private boolean paused = false;

	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 660, 660);
			root.setCenter(grid);
			snakeFood.setRadius(5);
			snakeFood.setFill(Color.WHITE);

			/*
			 * Creating the cell grids
			 */
			for (int row = 0; row < GRID_SIZE; row++) {
				for (int col = 0; col < GRID_SIZE; col++) {
					Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
					cell.setFill(Color.BLACK);
					cell.setStroke(Color.BLACK);
					grid.add(cell, col, row);
				}
			}

			snakeHead.setFill(Color.WHITE);
			// Adding the snake head and food to the grid
			grid.add(snakeHead, currentRow, currentCol);
			grid.add(snakeFood, randomX, randomY);
			/*
			 * Changing direction when arrow keys are pressed Pressing R key restarts the
			 * game after game over Pressing SPACE key pauses the game
			 */
			scene.setOnKeyPressed(event -> {
				Direction currentDirection = direction;
				switch (event.getCode()) {
				case UP:
					if (currentDirection != Direction.DOWN) {

						direction = Direction.UP;
					}
					break;
				case DOWN:
					if (currentDirection != Direction.UP) {

						direction = Direction.DOWN;
					}
					break;
				case LEFT:
					if (currentDirection != Direction.RIGHT) {

						direction = Direction.LEFT;
					}
					break;
				case RIGHT:
					if (currentDirection != Direction.LEFT) {

						direction = Direction.RIGHT;
					}
					break;
				case SPACE:
					if (event.getCode() == KeyCode.SPACE) {
						if (paused) {
							resumeGame();
						} else {
							pauseGame();
						}
					}
					break;
				case R:
					if (event.getCode() == KeyCode.R) {
						if (checkSelfCollision()) {

							restartGame(grid, snakeFood);
						}

					}
				default:
					break;
				}
			});

			timeline = new Timeline(new KeyFrame(Duration.millis(1000 / SPEED), event -> {
				switch (direction) {
				case UP:
					currentRow--;
					break;
				case DOWN:
					currentRow++;
					break;
				case LEFT:
					currentCol--;
					break;
				case RIGHT:
					currentCol++;
					break;

				}
				currentCol = (currentCol + GRID_SIZE) % GRID_SIZE;
				currentRow = (currentRow + GRID_SIZE) % GRID_SIZE;

				GridPane.setColumnIndex(snakeHead, currentCol);
				GridPane.setRowIndex(snakeHead, currentRow);

				moveTail();

			}));
			timeline.setCycleCount(Timeline.INDEFINITE);
			timeline.play();
			/*
			 * Animation Timer checks for collisions, removes the food,makes the food appear
			 * in another random place on the grid and grows the snake This section also
			 * checks if the food appears where the snake is also occupying
			 */
			timer = new AnimationTimer() {
				long lastUpdate = 0;

				@Override
				public void handle(long now) {
					if (now - lastUpdate >= 20_000_000) {
						// Check collision
						if (snakeHead.getBoundsInParent().intersects(snakeFood.getBoundsInParent())) {
							// Handle collision
							collisionDetected = true;
							// remove the food
							grid.getChildren().remove(snakeFood);

							int foodX, foodY;// initialize variables to check if next position is occupied by snake
							do {
								foodX = random.nextInt(GRID_SIZE);
								foodY = random.nextInt(GRID_SIZE);
							} while (occupiedBySnake(foodX, foodY));// call function to check if grid is occupied by the
																	// snake

							grid.add(snakeFood, foodX, foodY);// place the snake in the next position
							// Handling the snake growth by increasing tail size:
							Rectangle newTailSegment = new Rectangle(CELL_SIZE, CELL_SIZE);
							newTailSegment.setFill(Color.WHITE);
							switch (direction) {
							case UP:
								tailSegment.add(newTailSegment);
								grid.add(newTailSegment, currentCol, currentRow + 1);
								break;
							case DOWN:
								tailSegment.add(newTailSegment);
								grid.add(newTailSegment, currentCol, currentRow - 1);
								break;
							case LEFT:
								tailSegment.add(newTailSegment);
								grid.add(newTailSegment, currentCol + 1, currentRow);
								break;
							case RIGHT:
								tailSegment.add(newTailSegment);
								grid.add(newTailSegment, currentCol - 1, currentRow);
								break;
							}
						} else {
							collisionDetected = false;
						}

						lastUpdate = now;
					}
				}
			};
			timer.start();

			/*
			 * Animation timer to continuously check for collision between the snake and its
			 * body
			 */

			AnimationTimer selfCollisionTimer = new AnimationTimer() {
				long lastUpdate = 0;

				@Override
				public void handle(long now) {
					if (now - lastUpdate >= 20_000_000) {
						// Call checkSelfCollision method to check for collision
						if (checkSelfCollision()) {
							// If there is a collision, stop the timeline and the timer
							timeline.stop();
							timer.stop();
						}
						lastUpdate = now;
					}
				}
			};
			selfCollisionTimer.start();

			root.layout();
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * enums to represent arrow keys
	 */
	private enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	/*
	 * Handling tail movements
	 */
	private void moveTail() {

		for (int i = tailSegment.size() - 1; i > 0; i--) {
			Rectangle currentSegment = tailSegment.get(i);
			currentSegment.setFill(Color.WHITE);
			Rectangle prevSegment = tailSegment.get(i - 1);
			GridPane.setColumnIndex(currentSegment, GridPane.getColumnIndex(prevSegment));
			GridPane.setRowIndex(currentSegment, GridPane.getRowIndex(prevSegment));

		}
		if (!tailSegment.isEmpty()) {
			Rectangle firstSegment = tailSegment.get(0);
			GridPane.setColumnIndex(firstSegment, currentCol);
			GridPane.setRowIndex(firstSegment, currentRow);
		}

	}

	/*
	 * Check if body segment collides with snake head
	 */
	private boolean checkSelfCollision() {
		for (int i = 1; i < tailSegment.size(); i++) {
			Rectangle segment = tailSegment.get(i);
			if (segment.getBoundsInParent().intersects(snakeHead.getBoundsInParent())) {
				return true; // Collision detected
			}
		}
		return false; // No collision detected
	}

	/*
	 * Check whether the next random position is occupied by the snake
	 */

	private boolean occupiedBySnake(int x, int y) {
		// Check if food is on the snake's head or tail
		if ((x == currentCol && y == currentRow) || tailSegment.stream()
				.anyMatch(segment -> GridPane.getColumnIndex(segment) == x && GridPane.getRowIndex(segment) == y)) {
			return true;
		}
		return false;
	}

	/*
	 * Method for pausing the game
	 */
	private void pauseGame() {
		paused = true;
		timeline.stop();
		timer.stop();
	}

	// Method to resume the game
	private void resumeGame() {
		paused = false;
		timeline.play();
		timer.start();
	}

	/*
	 * Method to restart the game
	 */
	private void restartGame(GridPane grid, Circle snakeFood) {
		// Reset snake position and direction
		currentCol = 4;
		currentRow = 5;
		direction = Direction.RIGHT;

		// Remove tail segments
		grid.getChildren().removeAll(tailSegment);
		tailSegment.clear();

		// Reset food position
		grid.getChildren().remove(snakeFood);
		int foodX, foodY;
		do {
			foodX = random.nextInt(GRID_SIZE);
			foodY = random.nextInt(GRID_SIZE);
		} while (occupiedBySnake(foodX, foodY));
		grid.add(snakeFood, foodX, foodY);

		// Restart timers
		timeline.play();
		timer.start();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
