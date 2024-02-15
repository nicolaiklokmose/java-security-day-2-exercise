package com.booleanuk.library.controllers;

import com.booleanuk.library.HelperUtils;
import com.booleanuk.library.models.Book;
import com.booleanuk.library.models.User;
import com.booleanuk.library.payload.response.*;
import com.booleanuk.library.repository.BookRepository;
import com.booleanuk.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("books")
public class BookController {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<BookListResponse> getAllBooks() {
        BookListResponse bookListResponse = new BookListResponse();
        bookListResponse.set(this.bookRepository.findAll());
        return ResponseEntity.ok(bookListResponse);
    }

    @PostMapping
    public ResponseEntity<Response<?>> createBook(@RequestBody Book book) {
        BookResponse bookResponse = new BookResponse();
        try {
            bookResponse.set(this.bookRepository.save(book));
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Bad request");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(bookResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<?>> getBookById(@PathVariable int id) {
        Book book = this.bookRepository.findById(id).orElse(null);
        if (book == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        BookResponse bookResponse = new BookResponse();
        bookResponse.set(book);
        return ResponseEntity.ok(bookResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<?>> updateBook(@PathVariable int id, @RequestBody Book book) {
        Book bookToUpdate = this.bookRepository.findById(id).orElse(null);
        if (bookToUpdate == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        bookToUpdate.setTitle(book.getTitle());
        bookToUpdate.setAuthor(book.getAuthor());
        bookToUpdate.setPublisher(book.getPublisher());
        bookToUpdate.setYear(book.getYear());
        bookToUpdate.setGenre(book.getGenre());

        try {
            bookToUpdate = this.bookRepository.save(bookToUpdate);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Bad request");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        BookResponse bookResponse = new BookResponse();
        bookResponse.set(bookToUpdate);
        return new ResponseEntity<>(bookResponse, HttpStatus.CREATED);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Response<?>> deleteBook(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Book bookToDelete = this.bookRepository.findById(id).orElse(null);
        if (bookToDelete == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        this.bookRepository.delete(bookToDelete);
        BookResponse bookResponse = new BookResponse();
        bookResponse.set(bookToDelete);
        return ResponseEntity.ok(bookResponse);
    }

    @PutMapping("/{bookId}/users/{username}")
    public ResponseEntity<ApiResponse<?>> borrowBookByBookIdAndUsername(@PathVariable int bookId, @PathVariable String username) {
        User userThatBorrows = this.userRepository.findByUsername(username).orElseThrow();
        Book bookToBorrow = this.bookRepository.findById(bookId).orElseThrow();
        userThatBorrows.getBooks().add(bookToBorrow);
        this.userRepository.save(userThatBorrows);
        return HelperUtils.createdRequest(userThatBorrows);
    }
}
