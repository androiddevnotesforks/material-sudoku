package com.jdamcd.sudoku.repository

import com.jdamcd.sudoku.repository.database.PuzzleDao
import com.jdamcd.sudoku.repository.database.PuzzleSave
import com.jdamcd.sudoku.util.Strings
import com.jdamcd.sudoku.util.randomElement
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class PuzzleRepository
    @Inject
    constructor(
        private val dao: PuzzleDao,
        private val strings: Strings,
    ) {
        fun getPuzzle(id: Long): Single<Puzzle> =
            dao
                .getPuzzle(id)
                .map { it.toPuzzle(strings) }

        fun getPuzzles(
            level: Level,
            hideCompleted: Boolean,
        ): Flowable<List<Puzzle>> =
            dao
                .getPuzzles(level.id)
                .flatMap { list ->
                    Observable
                        .fromIterable(list)
                        .map { it.toPuzzle(strings) }
                        .filter { !it.isCompleted || !hideCompleted }
                        .toList()
                        .toFlowable()
                }

        fun getPuzzles(ids: Set<Long>): List<Puzzle> =
            dao
                .bulkGetPuzzles(ids)
                .asSequence()
                .map { it.toPuzzle(strings) }
                .toList()

        fun getBookmarkedPuzzles(): Flowable<List<Puzzle>> =
            dao
                .getBookmarkedPuzzles()
                .flatMap { list ->
                    Observable
                        .fromIterable(list)
                        .map { it.toPuzzle(strings) }
                        .toList()
                        .toFlowable()
                }

        fun getCompletedPuzzles(): Flowable<List<Puzzle>> =
            dao
                .getCompletedPuzzles()
                .flatMap { list ->
                    Observable
                        .fromIterable(list)
                        .map { it.toPuzzle(strings) }
                        .toList()
                        .toFlowable()
                }

        fun getRandomUnplayedPuzzleId(level: Level): Single<Long> =
            dao
                .getIncompletePuzzles(level.id)
                .flatMap { puzzles ->
                    Observable
                        .fromIterable(puzzles)
                        .filter { it.time == null || it.time == 0L }
                        .toList()
                }.map { it.randomElement().id }

        fun removeAllBookmarks(): Completable =
            Completable.fromAction {
                dao.removeAllBookmarks()
            }

        fun countCompleted(): Single<Int> = dao.countCompleted()

        fun save(puzzle: PuzzleSave): Completable =
            Completable.fromAction {
                dao.updatePuzzle(
                    puzzle.id,
                    puzzle.game,
                    puzzle.notes,
                    puzzle.time,
                    puzzle.bookmarked,
                    puzzle.progress,
                    puzzle.completed,
                    puzzle.cheats,
                )
            }

        fun save(puzzles: Set<PuzzleSave>): Completable =
            Completable.fromAction {
                dao.bulkUpdatePuzzles(puzzles)
            }

        fun setBookmarked(
            id: Long,
            isBookmarked: Boolean,
        ): Completable =
            Completable.fromAction {
                dao.updateBookmark(id, isBookmarked)
            }
    }
