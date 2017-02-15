/**
* @name             HomeViewController.swift
* @partof           zucred AG
* @description
* @author	 		Vasco Mouta
* @created			25.10.2015
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

import UIKit
import zucredApple
import GSMessages

open class HomeViewController: AppCenterViewController {

    static let DefaultJokeFileID = "sound.txt"
    static let DefaultJokeFile = "sound.caf"
    
    open lazy var jokeFileUrl: String = {
        let path = (AppProperties.sharedInstance.documentsDirectory as NSString?)?.stringByAppendingPathComponent(HomeViewController.DefaultJokeFile)
        return path!
    }()
    
    open lazy var jokeFileIDUrl: String = {
        let path = (AppProperties.sharedInstance.documentsDirectory as NSString?)?.stringByAppendingPathComponent(HomeViewController.DefaultJokeFileID)
        return path!
    }()
    
    open var currentJokeId: String? {
        return try? String(contentsOfFile:jokeFileIDUrl, encoding: String.Encoding.utf8)
    }
    
    var count = 0
    var hasNextJoke: Bool {
        if let jokes = JokrAppDelegate.sharedInstance.login?.recommendedJokes {
            if jokes.count-1 > count { return true }
            else if jokes.count > count {
                JokrAppDelegate.sharedInstance.fetchMoreJokes { _ in
                }
                return true
            } else {
                JokrAppDelegate.sharedInstance.fetchMoreJokes { _ in
                }
            }
        }
        return false
    }
    
    var getNextJoke: Joke? {
        if let jokes = JokrAppDelegate.sharedInstance.login?.recommendedJokes {
            if jokes.count > count { return jokes[count++] }
        }
        return nil
    }
    
    @IBOutlet weak var playJoke: UIButton!
    @IBOutlet weak var recordButton: UIButton!
    @IBOutlet weak var listenButton: UIButton!
    @IBOutlet weak var uploadButton: UIButton!
    
    @IBOutlet weak var titleImageView: UIImageView!
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        // Set Spiner font
        SwiftSpinner.setTitleFont(UIFont(name: "HelveticaNeue-UltraLight", size: 32.0))
        
        // Load User
        JokrAppDelegate.sharedInstance.loadLogin() { response in
            switch(response) {
            case .newAccount:
                    self.performSegueWithIdentifier("SETTINGS", sender: self)
                default:
                    break
            }
        }
        
        var image = UIImage(named: "IconMore")
        image = image?.withRenderingMode(UIImageRenderingMode.alwaysOriginal)
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(image: image, style: UIBarButtonItemStyle.Plain, target: self, action: "leftTapped:")
        self.navigationController?.navigationBar.barTintColor =  UIColor(red: 91.0/255.0, green: 155.0/255.0, blue: 213.0/255.0, alpha: 1.0)
        self.navigationController?.navigationBar.tintColor = UIColor.whiteColor()
        navigationItem.titleView = titleImageView
        
        checkRecordFile()
    }
    
    // For the moment since AppBackViewController doesn't support Ananlytics
    override open func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        Analytics.sharedInstance.createScreenView(self.className())
        Analytics.sharedInstance.logger.verbose("TrackUIViewController willAppear \(self.className())")
    }
    
    open override var hasLeftViewController: Bool {
        return true
    }

    open func checkRecordFile() -> Bool {
        let exist = FileManager.default.fileExists(atPath: jokeFileUrl)
        if (exist) {
            listenButton.isEnabled = true
            uploadButton.isEnabled = true
        } else {
            listenButton.isEnabled = false
            uploadButton.isEnabled = false
        }
        return exist
    }
    
    open func finishRecordingFile() {
        if(checkRecordFile()) {
            if let login = JokrAppDelegate.sharedInstance.login, let user = login.getUser(), user.serverID != nil {
                let joke = Joke(userId: user.serverID!, userName: user.userName, language: user.country ?? User.DefaultLanguage, filePath:jokeFileUrl)
                login.myJokes.append(joke)
                try! joke.modelID.writeToFile(jokeFileIDUrl, atomically: false, encoding: String.Encoding.utf8)
                login.save()
            }
        }
    }
    
    @IBAction func playJoke(_ sender: AnyObject) {
        if self.hasNextJoke {
            self.performSegueWithIdentifier("JOKE", sender: self)
        } else {
            
        }
    }
    
    @IBAction func starRecording(_ sender: AnyObject) {
        SwiftSpinner.show("Go").addTapHandler({
            AppAudio.sharedInstance.stopAudio()
            self.finishRecordingFile()
            SwiftSpinner.hide()
        }, subtitle: "Tap to stop recording.").fillOnTouch = true
        AppAudio.sharedInstance.startRecording(URL(fileURLWithPath: jokeFileUrl)) {_ in
            SwiftSpinner.hide()
            self.finishRecordingFile()
        }
    }
    
    @IBAction func listenJoke(_ sender: AnyObject) {
        SwiftSpinner.show("Nice...").addTapHandler({
            AppAudio.sharedInstance.stopAudio()
            SwiftSpinner.hide()
            }, subtitle: "Tap to stop playing.").fillOnTouch = true
        AppAudio.sharedInstance.playFileAtPath(jokeFileUrl) {_ in
            SwiftSpinner.hide()
        }
    }
    
    override open func prepareForSegue(_ segue: UIStoryboardSegue, sender: AnyObject?) {
        if let ident = segue.identifier, let navigationController = segue.destination as? UINavigationController {
            switch(ident) {
            case "UPLOAD":
                if let uploadViewController = navigationController.topViewController as? UploadViewController {
                    if let login = JokrAppDelegate.sharedInstance.login, let user = login.getUser(), user.serverID != nil {
                        if let theId = currentJokeId, let i = login.myJokes.indexOf({$0.modelID == theId}) {
                            uploadViewController.joke = login.myJokes[i]
                        } else if let userServerID = login.getUser()?.serverID {
                            uploadViewController.joke = Joke(userId: userServerID, userName: user.userName, language: user.country ?? User.DefaultLanguage, filePath:jokeFileUrl)
                        } else {
                            AppLogger.warning("No userServerID found, impossible to upload")
                        }
                    }
                    uploadViewController.logo = UIImage(named: "IconUpload")
                    uploadViewController.origin = .home
                }
            case "MYJOKES":
                if let jokesViewController = navigationController.topViewController as? JokesTableViewController, let myJokes = JokrAppDelegate.sharedInstance.login?.userJokes {
                    jokesViewController.user = JokrAppDelegate.sharedInstance.login?.getUser()
                    jokesViewController.type = .myJokes
                    jokesViewController.jokes = myJokes
                    jokesViewController.logo = UIImage(named: "IconMyJokes")
                }
            case "FAVORITES":
                if let jokesViewController = navigationController.topViewController as? JokesTableViewController, let favoriteJokes = JokrAppDelegate.sharedInstance.login?.favoriteJokes {
                    jokesViewController.type = .favorites
                    jokesViewController.jokes = favoriteJokes
                    jokesViewController.logo = UIImage(named: "IconFavorites")
                }
            case "RECOMMENDED":
                if let jokesViewController = navigationController.topViewController as? JokesTableViewController, let recommendedJokes = JokrAppDelegate.sharedInstance.login?.recommendedJokes {
                    jokesViewController.type = .recommended
                    jokesViewController.jokes = recommendedJokes
                    jokesViewController.logo = UIImage(named: "IconRecommended")
                }
            case "FOLLOWING":
                if let usersViewController = navigationController.topViewController as? UsersTableViewController, let followingUsers = JokrAppDelegate.sharedInstance.login?.followingUsers {
                    usersViewController.type = .following
                    usersViewController.users = followingUsers
                    usersViewController.logo = UIImage(named: "IconFollowing")
                }
            case "BLOCKED":
                if let usersViewController = navigationController.topViewController as? UsersTableViewController, let blockedUsers = JokrAppDelegate.sharedInstance.login?.blockedUsers {
                    usersViewController.type = .blocked
                    usersViewController.users = blockedUsers
                    usersViewController.logo = UIImage(named: "IconBlocked")
                }
            case "SETTINGS":
                if let settingsViewController = navigationController.topViewController as? SettingsTableViewController {
                    settingsViewController.logo = UIImage(named: "IconSettings")
                    settingsViewController.login = JokrAppDelegate.sharedInstance.login
                }
            default:
                break
            }
        } else if let jokeViewController = segue.destination as? JokeViewController, segue.identifier == "JOKE" {
            jokeViewController.joke = self.getNextJoke
        }
    }

    @IBAction open func done(_ segue:UIStoryboardSegue) {
        
    }
    
    @IBAction open func cancel(_ segue:UIStoryboardSegue) {
        
    }
    
    @IBAction open func uploadUpload(_ segue:UIStoryboardSegue) {
        if let uploadViewController = segue.source as? UploadViewController, let joke = uploadViewController.joke  {
            SwiftSpinner.show("Uploading...").addTapHandler({
                AppAudio.sharedInstance.stopAudio()
                self.checkRecordFile()
                SwiftSpinner.hide()
            }, subtitle: "Tap to cancel.").fillOnTouch = true
            JokrConnection.sharedInstance.uploadJoke(joke){result in
                SwiftSpinner.hide()
                switch result {
                case .Success(_):
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.MyJokes)
                    self.showMessage("Joke uploaded!", type: .Success, options:nil)
                case .Failure(_):
                    self.showMessage("Fail to uploaded!", type: .Error, options:nil)
                }
            }
        } else {
            AppLogger.error("upload call without right conditions")
        }
    }
    
    @IBAction open func settingsSave(_ segue:UIStoryboardSegue) {
        if let settingsViewController = segue.source as? SettingsTableViewController {
            if let login = settingsViewController.newLogin {
                JokrAppDelegate.sharedInstance.updateLogin(login)
                if let back = leftViewController as? MoreViewController {
                    back.updateImage()
                }
            }
        }
    }
}

// MARK: - From BackViewControllerDelegate */
extension HomeViewController {
    
    public override func execute(_ identifier: String) {
        //delegate?.collapseSidePanels?()
        switch(identifier) {
            case "Settings":
                self.performSegueWithIdentifier("SETTINGS", sender: self)
            case "MyJokes":
                if let myJokes = JokrAppDelegate.sharedInstance.login?.userJokes, myJokes.count > 0 {
                    self.performSegueWithIdentifier("MYJOKES", sender: self)
                } else {
                    leftViewController?.view.showMessage("You have no Jokes", type: .Warning, options: [.TextPadding(5), .Height(64),.TextAlignment(.Left)])
                }
            case "Favorite":
                if let favoriteJokes = JokrAppDelegate.sharedInstance.login?.favoriteJokes, favoriteJokes.count > 0 {
                    self.performSegueWithIdentifier("FAVORITES", sender: self)
                } else {
                    leftViewController?.view.showMessage("You have no favorites", type: .Warning, options: [.TextPadding(5), .Height(64),.TextAlignment(.Left)])
                }
            case "Recommended":
                if let recommendedJokes = JokrAppDelegate.sharedInstance.login?.recommendedJokes, recommendedJokes.count > 0 {
                    self.performSegueWithIdentifier("RECOMMENDED", sender: self)
                } else {
                    leftViewController?.view.showMessage("You have no recomendations", type: .Warning, options: [.TextPadding(5), .Height(64),.TextAlignment(.Left)])
                }
                break
            case "Following":
                if let followingUsers = JokrAppDelegate.sharedInstance.login?.followingUsers, followingUsers.count > 0 {
                    self.performSegueWithIdentifier("FOLLOWING", sender: self)
                } else {
                    leftViewController?.view.showMessage("You are not following any user", type: .Warning, options: [.TextPadding(5), .Height(64),.TextAlignment(.Left)])
                }
            case "Blocked":
                if let blockedUsers = JokrAppDelegate.sharedInstance.login?.blockedUsers, blockedUsers.count > 0 {
                    self.performSegueWithIdentifier("BLOCKED", sender: self)
                } else {
                    leftViewController?.view.showMessage("You didn't block any user", type: .Warning, options: [.TextPadding(5), .Height(64), .TextAlignment(.Left)])
                }
            default:
                break
        }
    }
}
