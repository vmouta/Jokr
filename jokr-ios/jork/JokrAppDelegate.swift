/**
 * @name             JokrAppDelegate.swift
 * @partof           zucred AG
 * @description
 * @author	 		Vasco Mouta
 * @created			21/11/15
 *
 * Copyright (c) 2015 zucred AG All rights reserved.
 * This material, including documentation and any related
 * computer programs, is protected by copyright controlled by
 * zucred AG. All rights are reserved. Copying,
 * including reproducing, storing, adapting or translating, any
 * or all of this material requires the prior written consent of
 * zucred AG. This material also contains confidential
 * information which may not be disclosed to others without the
 * prior written consent of zucred AG.
 */

import Foundation
import zucredApple

public struct UpdateOptions : OptionSet, CustomStringConvertible {
    
    fileprivate enum UpdateOption : Int, CustomStringConvertible {
        case myJokes=0b00001, favorites=0b00010, following=0b00100, blocked=0b01000, profilePic=0b10000, recommendedJokes=0b100000
        
        var description : String {
            var shift = 0
            while (rawValue >> shift != 1){ shift += 1 }
            return ["MyJokes","Favorites","Following","Blocked","ProfilePic","RecommendedJokes"][shift]
        }
    }
    
    public let rawValue: Int
    public init(rawValue: Int) { self.rawValue = rawValue }
    fileprivate init(_ oprtion:UpdateOption){ self.rawValue = oprtion.rawValue }
    
    
    public static let None             = UpdateOptions(rawValue: 0)
    public static let MyJokes          = UpdateOptions(UpdateOption.myJokes)
    public static let Favorites        = UpdateOptions(UpdateOption.favorites)
    public static let Following        = UpdateOptions(UpdateOption.following)
    public static let Blocked          = UpdateOptions(UpdateOption.blocked)
    public static let ProfilePic       = UpdateOptions(UpdateOption.profilePic)
    public static let RecommendedJokes = UpdateOptions(UpdateOption.recommendedJokes)
    
    public static let StartApp: UpdateOptions   = [MyJokes, Favorites, Following, Blocked, RecommendedJokes]
    public static let All                       = UpdateOptions(rawValue: 0b111111)
    
    // MARK: BooleanType
    
    public var boolValue: Bool {
        return rawValue != 0
    }
    
    // MARK: CustomStringConvertible
    
    public var description : String{
        var result = ""
        var shift = 0
        while let currentDirection = UpdateOption(rawValue: 1 << shift++){
            if self.contains(UpdateOptions(currentDirection)){
                result += (result.characters.count == 0) ? "\(currentDirection)" : ",\(currentDirection)"
            }
        }
        
        return "[\(result)]"
    }
}

@UIApplicationMain
open class JokrAppDelegate: AppDelegate {
    
    open var login: Login?
    open var sessionID: String = AppProperties.sharedInstance.getUniqueID
    
    open override func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [AnyHashable: Any]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions:launchOptions)
        UINavigationBar.appearance().barStyle = .Black
        return true
    }
    
    open typealias CompletionBlock = ()->Void
    open func updateApp(_ options: UpdateOptions, updateCompleted:CompletionBlock? = nil) {
        logger.info("Update options: \(options)")
        
        var updated:UpdateOptions = .None
        if let user = self.login?.getUser() {
            
            if(options.contains(.RecommendedJokes)) {
                fetchMoreJokes(true) { _ in
                    updated.insert(.RecommendedJokes)
                    if updated == options {
                        updateCompleted?()
                    }
                }
            }
            
            if(options.contains(.MyJokes)) {
                JokrConnection.sharedInstance.fetchUserJokes(user) {result in
                    switch(result) {
                    case .Success(let jokes):
                        self.logger.debug("Got \(jokes.count) user jokes")
                        self.login?.myServerJokes = jokes
                    case .Failure(_):
                        break
                    }
                    updated.insert(.MyJokes)
                    if updated == options {
                        updateCompleted?()
                    }
                }
            }
            
            if(options.contains(.Favorites)) {
                JokrConnection.sharedInstance.getAllFavoritesOfUser(user) {result in
                    switch(result) {
                    case .Success(let jokes):
                        self.logger.debug("Got \(jokes.count) favorite jokes")
                        self.login?.favoriteJokes = jokes
                    case .Failure(_):
                        break
                    }
                    updated.insert(.Favorites)
                    if updated == options {
                        updateCompleted?()
                    }
                }
            }
            
            if(options.contains(.Following)) {
                JokrConnection.sharedInstance.getAllFollowUsers(user) {result in
                    switch(result) {
                    case .Success(let users):
                        self.logger.debug("Got \(users.count) followers jokes")
                        self.login?.followingUsers = users
                    case .Failure(_):
                        break
                    }
                    updated.insert(.Following)
                    if updated == options {
                        updateCompleted?()
                    }
                }
            }
            
            if(options.contains(.Blocked)) {
                JokrConnection.sharedInstance.getAllBlockedUsers(user) {result in
                    switch(result) {
                    case .Success(let users):
                        self.logger.debug("Got \(users.count) Blocked jokes")
                        self.login?.blockedUsers = users
                    case .Failure(_):
                        break
                    }
                    updated.insert(.Blocked)
                    if updated == options {
                        updateCompleted?()
                    }
                }
            }
            
            if(options.contains(.ProfilePic)) {
                if let userServerID = user.serverID {
                    JokrConnection.sharedInstance.downloadProfilePicure(userServerID){result in
                        switch(result) {
                        case .Success(let picture):
                            self.login!.setLoginUserImage(picture) { _ in }
                            self.logger.debug("Got Picture for \(userServerID)")
                        case .Failure(_):
                            break
                        }
                        updated.insert(.ProfilePic)
                        if updated == options {
                            updateCompleted?()
                        }
                    }
                } else {
                    self.logger.warning("No user.serverID present")
                    updated.insert(.ProfilePic)
                    if updated == options {
                        updateCompleted?()
                    }
                }
            }
            
        } else {
            self.logger.severe("No user present")
            updateCompleted?()
        }
    }
    
    func fetchMoreJokes(_ reset:Bool = false, handler: (Bool)->Void) {
        if let user = self.login?.getUser() {
            JokrConnection.sharedInstance.fetchRecommendedJokes(user, sessionID: self.sessionID) {result in
                switch(result) {
                case .Success(let jokes):
                    self.logger.debug("Got \(jokes.count) recommended jokes")
                    (reset==true ? self.login?.recommendedJokes=jokes : self.login?.recommendedJokes.appendContentsOf(jokes))
                    
                    handler(true)
                case .Failure(_):
                    handler(false)
                    break
                }
            }
        }
        handler(false)
    }
}

// MARK: Login
extension JokrAppDelegate {
    
    public enum LoginResult {
        case newAccount
        case restoredUser
        case loadLocaly
        case fail()
    }
    
    public func loadLogin(_ loginHandler: @escaping (LoginResult)-> Void) {
        logger.verbose()
        let logins = Login.listAll(Login)
        if logins.count == 0 {
        // try to load user from server
            SwiftSpinner.show("Registering...")
            
            // Create user
            let temp = Login(deviceID: AppProperties.sharedInstance.deviceID!)
            JokrConnection.sharedInstance.createUser(temp) { result in
                switch(result) {
                case .Success(let login):
                    if  let register = temp.getUser()?.creationTimestamp, register > login.getUser()?.creationTimestamp {
                        // We have a old user lets just update
                        self.login = login
                        self.login!.status = SyncStatus.Sync.rawValue
                        self.login!.save()
                        self.updateApp(UpdateOptions.ProfilePic)
                        self.succeededLogin()
                        loginHandler(.RestoredUser)
                    } else {
                        // We have a new user lets set settings before moving on
                        self.login = login
                        loginHandler(.NewAccount)
                        SwiftSpinner.hide()
                    }
                case .Failure(let error):
                    self.failLogin(error)
                    loginHandler(.Fail())
                    SwiftSpinner.hide()
                }
            }
        } else {
        // We have an user
            self.login = logins.first!
            if(self.login!.status == SyncStatus.New.rawValue) {
            // A new user means no server id
                JokrConnection.sharedInstance.createUser(self.login!) { result in
                    switch(result) {
                    case .Success(let login):
                        if let newUserID = login.getUser()?.serverID {
                            self.login!.getUser()?.serverID = newUserID
                            self.login!.status = SyncStatus.Sync.rawValue
                            self.login!.save()
                            self.succeededLogin()
                        }
                    case .Failure(let error):
                        self.logger.severe("Fail to connect to the server and update user! \n\(error)")
                    }
                    loginHandler(.LoadLocaly)
                }
            } else if(self.login!.status == SyncStatus.NotSync.rawValue) {
            // with id but not sync
                JokrConnection.sharedInstance.createUser(self.login!) { result in
                    switch(result) {
                    case .Success(_):
                        self.login!.status = SyncStatus.Sync.rawValue
                        self.login!.save()
                        self.succeededLogin()
                    case .Failure(let error):
                        self.logger.severe("Fail to connect to the server and update user! \n\(error)")
                    }
                    loginHandler(.LoadLocaly)
                }
            } else {
                self.succeededLogin()
                loginHandler(.loadLocaly)
            }
        }
    }
    
    public func updateLogin(_ login:Login) {
        logger.verbose()
        self.login = login
        self.login!.status = SyncStatus.NotSync.rawValue
        self.login!.save()
        JokrConnection.sharedInstance.createUser(self.login!) { result in
            switch(result) {
            case .Success(_):
                self.login!.status = SyncStatus.Sync.rawValue
                self.login!.save()
            case .Failure(let error):
                self.logger.severe("Fail to connect to the server and update user! \n\(error)")
            }
        }
        // TODO: Server implementation of the update user
        self.succeededLogin()
    }
    
    public func succeededLogin() {
        logger.verbose()
        SwiftSpinner.show("Loading...")
        updateApp(UpdateOptions.StartApp, updateCompleted:completeLogin)
    }
    
    fileprivate func failLogin(_ error: Error) {
        self.logger.severe("Fail to connect to the server and no user stored better jump out! \n\(error)")
    }
    
    fileprivate func completeLogin() {
        logger.verbose()
        SwiftSpinner.hide()
    }
}

// MARK: singletons extension
extension JokrAppDelegate {
    
    public override class var sharedInstance: JokrAppDelegate {
        return UIApplication.shared.delegate as! JokrAppDelegate
    }
    
    public override class func initSingletons() {
        JokrConnection.sharedInstance
        JokrDB.sharedInstance
        super.initSingletons()
    }
}
