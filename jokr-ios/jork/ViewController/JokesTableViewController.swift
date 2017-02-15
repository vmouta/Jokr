/**
 * @name             JokesTableViewController.swift
 * @partof           zucred AG
 * @description
 * @author	 		Vasco Mouta
 * @created			03/01/16
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
import UIKit
import zucredApple

public enum TypeView {
    case myJokes
    case favorites
    case recommended
    case userJokes
}

open class JokesTableViewController: TrackUITableViewController {
    
    open var jokes: [Joke]?
    open var user: User?
    open var logo: UIImage?
    
    open var type: TypeView = .myJokes
    
    var currentIndex:Int = 0
    var selectedJokeIndex:Int = 0
    
    @IBOutlet weak var barButton: UIBarButtonItem!
    @IBOutlet weak var doneBarButton: UIBarButtonItem!
    @IBOutlet weak var deleteBarButton: UIBarButtonItem!
    
    override open func viewDidLoad() {
        super.viewDidLoad()

        let imageView = UIImageView(image:logo)
        self.navigationItem.titleView = imageView
        self.navigationController?.navigationBar.tintColor = UIColor.white
        
        if type == .userJokes {
            self.deleteBarButton.isEnabled = false
            self.navigationItem.leftBarButtonItem = nil
            self.tableView.reloadData()
            self.refreshControl?.beginRefreshing()
            self.tableView.contentOffset = CGPoint(x: 0, y: -(self.refreshControl?.frame.size.height ?? 0));
            refreshData(self)
        } else {
            self.deleteBarButton.isEnabled = true
            self.navigationItem.leftBarButtonItem = doneBarButton
        }
    }
    
    override public var preferredStatusBarStyle : UIStatusBarStyle {
        return UIStatusBarStyle.lightContent
    }
    
    override open func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let jokeViewController = segue.destination as? JokeViewController where segue.identifier == "JOKE" {
            if(type == .myJokes) {
                jokeViewController.joke = jokes?[selectedJokeIndex-1]
            } else {
                jokeViewController.joke = jokes?[selectedJokeIndex]
            }
            tableView.deselectRow(at: IndexPath(row:selectedJokeIndex, section:0), animated: true)
        } else if let uploadViewController = segue.destination as? UploadViewController where segue.identifier == "UPLOAD" {
            uploadViewController.joke = jokes?[selectedJokeIndex]
            uploadViewController.logo = UIImage(named: "IconUpload")
            uploadViewController.origin = .jokes
        }
    }
    
    @IBAction func editJokes(_ sender: AnyObject) {
        self.tableView.setEditing(!self.tableView.isEditing, animated: true)
    }
    
    @IBAction func playStopJokes(_ sender: AnyObject) {
        if barButton.tag == 1 {
            barButton.image = UIImage(named:"IconStop")
            barButton.tag = 0
            playJoke()
        } else {
            barButton.image = UIImage(named:"IconPlay")
            barButton.tag = 1
            stopPlaying()
        }
        self.tableView.beginUpdates();
        self.tableView.reloadRows(at: [IndexPath(row:self.currentIndex+(self.type == .myJokes ? 1 : 0), section:0)], with:.none)
        self.tableView.endUpdates();
    }
    
    func playJoke() {
        if let joke = jokes?[currentIndex] {
            if joke.status == SyncStatus.NotSync.rawValue {
                AppAudio.sharedInstance.playFileAtPath(joke.localUrl!, name:joke.title , author:joke.userName) { _ in
                    if ++self.currentIndex<self.jokes?.count {
                        self.playJoke()
                        self.tableView.beginUpdates();
                        self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow:self.currentIndex, inSection:0), NSIndexPath(forRow:self.currentIndex, inSection:0)], withRowAnimation:.None)
                        self.tableView.endUpdates();
                    } else {
                        self.tableView.beginUpdates();
                        self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow:self.currentIndex+(self.type == .MyJokes ? 1 : 0), inSection:0)], withRowAnimation:.None)
                        self.tableView.endUpdates();
                        self.currentIndex = 0
                        self.playStopJokes(self)
                    }
                }
            } else {
                AppAudio.sharedInstance.streamFile(JokrConnection.Router.FetchJoke(jokeID: joke.modelID).URL, name:joke.title , author:joke.userName) { _ in
                    if ++self.currentIndex<self.jokes?.count {
                        self.playJoke()
                        self.tableView.beginUpdates();
                        self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow:self.currentIndex+(self.type == .MyJokes ? 0 : -1), inSection:0), NSIndexPath(forRow:self.currentIndex+(self.type == .MyJokes ? 1 : 0), inSection:0)], withRowAnimation:.None)
                        self.tableView.endUpdates();
                    } else {
                        self.tableView.beginUpdates();
                        self.tableView.reloadRowsAtIndexPaths([NSIndexPath(forRow:self.currentIndex+(self.type == .MyJokes ? 0 : -1), inSection:0)], withRowAnimation:.None)
                        self.tableView.endUpdates();
                        self.currentIndex = 0
                        self.playStopJokes(self)
                    }
                }
            }
        }
    }
    
    func stopPlaying() {
        AppAudio.sharedInstance.stopAudio()
    }
}

// MARK: Table View Data Source

extension JokesTableViewController {
    
    override public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        var nRows = jokes?.count ?? 0
        nRows += (type == .myJokes ? 1 : 0)
        return nRows
    }
    
    override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var returnCell: UITableViewCell?
        switch(type) {
            case .myJokes:
                if indexPath.row == 0 {
                    let cell = tableView.dequeueReusableCell(withIdentifier: "StatusCell", for: indexPath) as! StatusCell
                    if let login = JokrAppDelegate.sharedInstance.login {
                        cell.userName.text = login.getUser()?.name
                        cell.followers.text = String(login.getUser()?.numberOfFollowers ?? 0)
                        cell.following.text = String(login.followingUsers.count)
                        cell.jokesCount.text = String(login.myServerJokes.count)
                        cell.userImage.image = login.getLoginUserImage()
                        if let country = login.getUser()?.country {
                            cell.setCountry(country)
                        }
                    }
                    returnCell = cell
                } else if let joke = jokes?[indexPath.row-1] {
                    let cell = tableView.dequeueReusableCell(withIdentifier: "MyJokeCell", for: indexPath) as! MyJokeCell
                    cell.setJoke(joke)
                    if joke.isLocal {
                        cell.uploadButton.isHidden = false
                        cell.shareButton.isHidden = true
                    } else {
                        cell.uploadButton.isHidden = true
                        cell.shareButton.isHidden = false
                    }
                    cell.jokeIndex = indexPath.row-1
                    cell.playingImage.isHidden = (currentIndex != indexPath.row-1 || barButton.tag == 1)
                    returnCell = cell
                } else {
                    AppLogger.error("Index for a joke is out of bounds, we recover by give an empty cell")
                    returnCell = UITableViewCell()
                }
            case .favorites:
                if let joke = jokes?[indexPath.row] {
                    let cell = tableView.dequeueReusableCell(withIdentifier: "JokeCell", for: indexPath) as! JokeCell
                    cell.setJoke(joke)
                    JokrConnection.sharedInstance.downloadProfilePicure(joke.userId){result in
                        switch(result) {
                        case .Success(let picture):
                            cell.userImage.image = picture
                            break
                        case .Failure(_):
                            break
                        }
                    }
                    cell.favoriteButton.isHidden = true
                    cell.playingImage.isHidden = (currentIndex != indexPath.row || barButton.tag == 1)
                    cell.jokeIndex = indexPath.row
                    returnCell = cell
                } else {
                    AppLogger.error("Index for a joke is out of bounds, we recover by give an empty cell")
                    returnCell = UITableViewCell()
                }
            case .recommended:
                if let joke = jokes?[indexPath.row] {
                    let cell = tableView.dequeueReusableCell(withIdentifier: "JokeCell", for: indexPath) as! JokeCell
                    cell.setJoke(joke)
                    JokrConnection.sharedInstance.downloadProfilePicure(joke.userId){result in
                        switch(result) {
                        case .Success(let picture):
                            cell.userImage.image = picture
                            break
                        case .Failure(_):
                            break
                        }
                    }
                    if let login = JokrAppDelegate.sharedInstance.login {
                        cell.favoriteButton.selected = login.hasFavorite(joke.modelID)
                        cell.favoriteButton.isHidden = false
                    } else {
                        cell.favoriteButton.isHidden = true
                    }
                    cell.playingImage.isHidden = (currentIndex != indexPath.row || barButton.tag == 1)
                    cell.jokeIndex = indexPath.row
                    returnCell = cell
                } else {
                    AppLogger.error("Index for a joke is out of bounds, we recover by give an empty cell")
                    returnCell = UITableViewCell()
                }
            case .userJokes:
                if let joke = jokes?[indexPath.row] {
                    let cell = tableView.dequeueReusableCell(withIdentifier: "MyJokeCell", for: indexPath) as! MyJokeCell
                    cell.setJoke(joke)
                    cell.uploadButton.isHidden = true
                    cell.playingImage.isHidden = (currentIndex != indexPath.row || barButton.tag == 1)
                    cell.jokeIndex = indexPath.row
                    returnCell = cell
                } else {
                    AppLogger.error("Index for a joke is out of bounds, we recover by give an empty cell")
                    returnCell = UITableViewCell()
                }
        }
        return returnCell!
    }
    
    override public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        switch(type) {
        case .myJokes, .userJokes:
            return 85
        default:
            return 105
        }
    }
}

// Mark: Table View Delegate

extension JokesTableViewController {
    
    override public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if(type != .myJokes || indexPath.row != 0) {
            selectedJokeIndex = indexPath.row
            self.performSegue(withIdentifier: "JOKE", sender: self)
        }
    }
    
    override public func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool
    {
        if type == .userJokes {
             return false
        } else if(type != .myJokes || indexPath.row != 0) {
            return true
        } else {
            return false
        }
    }
    
    override public func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == UITableViewCellEditingStyle.delete {
            switch(type) {
            case .myJokes:
                if let removeJoke = jokes?.remove(at: indexPath.row-1) {
                    if removeJoke.isLocal {
                        if let url = removeJoke.localUrl {
                            do {
                                try FileManager.default.removeItem(atPath: url)
                                AppLogger.debug("Deleted local Joke file:\n\(url)")
                            } catch {
                                AppLogger.error("Fail to delete local Joke:\n\(url)")
                            }
                        } else {
                            AppLogger.error("No local file for local joke:\n\(removeJoke)")
                        }
                        if let login = JokrAppDelegate.sharedInstance.login, i = login.myJokes.indexOf({$0.modelID == removeJoke.modelID}) {
                            login.myJokes.removeAtIndex(i)
                            login.save()
                        }
                    } else {
                        JokrConnection.sharedInstance.deleteJoke(removeJoke.modelID) {_ in
                            JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.MyJokes)
                        }
                    }
                }
            case .favorites:
                let jokeToRemove = jokes?.remove(at: indexPath.row)
                if let loginID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID, jokeID = jokeToRemove?.modelID {
                    JokrConnection.sharedInstance.removeFavorite(loginID, jokeId:jokeID) {_ in
                        JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Favorites)
                    }
                }
            case .recommended:
                jokes?.remove(at: indexPath.row)
            case .userJokes:
                AppLogger.warning("We shouldn't be trying to delete other users jokes")
                break
            }
            tableView.deleteRows(at: [indexPath], with: UITableViewRowAnimation.automatic)
        }
    }
}

// Mark: Handling refreshing request

extension JokesTableViewController {
    
    @IBAction func refreshData(_ sender: AnyObject) {
        switch(type) {
            case .myJokes:
                JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.MyJokes){
                    self.refreshControl?.endRefreshing()
                    self.jokes = JokrAppDelegate.sharedInstance.login?.userJokes
                    self.tableView.reloadData()
                }
            case .favorites:
                JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Favorites){
                    self.refreshControl?.endRefreshing()
                    self.jokes = JokrAppDelegate.sharedInstance.login?.favoriteJokes
                    self.tableView.reloadData()
                }
            case .recommended:
                JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.RecommendedJokes){
                    self.refreshControl?.endRefreshing()
                    self.jokes = JokrAppDelegate.sharedInstance.login?.recommendedJokes
                    self.tableView.reloadData()
                }
            case .userJokes:
                JokrConnection.sharedInstance.fetchUserJokes(user!) { result in
                    switch(result) {
                    case .Success(let jokes):
                        AppLogger.debug("Got \(jokes.count) for user \(self.user?.userName)")
                        self.jokes = jokes
                        self.tableView.reloadData()
                    case .Failure(_):
                        break
                    }
                    self.refreshControl?.endRefreshing()
                }
        }
    }
}

// MARK: CellDelegate

extension JokesTableViewController {

    func setFavorites(_ jokeIndex: Int, isFavorite:Bool) {
        if let jokeID = jokes?[jokeIndex].modelID, let userdID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID {
            if(isFavorite) {
                JokrConnection.sharedInstance.addFavorite(userdID, jokeId:jokeID) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Favorites)
                }
            } else {
                JokrConnection.sharedInstance.removeFavorite(userdID, jokeId:jokeID) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Favorites)
                }
            }
        } else {
            AppLogger.error("Not possible to change favorite due to nil jokeid or login serverID")
        }
    }
    
    func uploadJoke(_ jokeIndex: Int) {
        self.selectedJokeIndex = jokeIndex
        self.performSegue(withIdentifier: "UPLOAD", sender: self)
    }
    
    func shareJoke(_ jokeIndex: Int) {
        if let shareID = jokes?[jokeIndex].shareID where jokes?[jokeIndex].isLocal == false {
            let vc = UIActivityViewController(activityItems: [JokrConnection.Router.fetchJoke(jokeID: shareID).URL], applicationActivities: [])
            present(vc, animated: true, completion: nil)
        } else {
            AppLogger.error("Not possible to share local joke or no jokeID")
        }
    }
}

class MyJokeCell: UITableViewCell {
    
    @IBOutlet weak var languageImage: UIImageView!
    
    @IBOutlet weak var jokeTitle: UILabel!
    @IBOutlet weak var jokeDate: UILabel!
    @IBOutlet weak var jokeTags: UILabel!
    @IBOutlet weak var jokeRate: UILabel!
    
    @IBOutlet weak var uploadButton: UIButton!
    @IBOutlet weak var shareButton: UIButton!
    
    @IBOutlet weak var playingImage: UIView!
    
    @IBOutlet weak var delegate: JokesTableViewController!
    
    var jokeIndex:Int?
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.jokeTags.sizeToFit()
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    func setJoke(_ joke: Joke) {
        self.jokeTitle.text = joke.title
        self.jokeTitle.text = joke.title
        self.jokeDate.text = joke.timeSinceCreation
        self.jokeRate.text = joke.likes.description
        self.jokeTags.text = joke.tags
        setLanguage(joke.language)
    }
    
    func setLanguage(_ language: String) {
        switch(language) {
        case "en":
            languageImage.image = UIImage(named:"country_en")
        case "sde":
            languageImage.image = UIImage(named:"country_ch")
        case "de":
            languageImage.image = UIImage(named:"country_de")
        case "fr":
            languageImage.image = UIImage(named:"country_fr")
        case "it":
            languageImage.image = UIImage(named:"country_it")
        case "es":
            languageImage.image = UIImage(named:"country_es")
        case "pt":
            languageImage.image = UIImage(named:"country_pt")
        default:
            //TODO: Set a default image
            languageImage.image = nil
            break
        }
    }
    
    @IBAction func uploadJoke(_ sender: AnyObject){
        if self.jokeIndex != nil {delegate.uploadJoke(self.jokeIndex!)}
    }
    
    @IBAction func shareJoke(_ sender: AnyObject){
        if self.jokeIndex != nil {delegate.shareJoke(self.jokeIndex!)}
    }
}

class JokeCell: MyJokeCell {
    
    @IBOutlet weak var userName: UILabel!
    @IBOutlet weak var userImage: UIImageView!
    @IBOutlet weak var userCountry: UIImageView!
    
    @IBOutlet weak var countryImage: UIImageView!
    
    @IBOutlet weak var favoriteButton: UIButton!
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        userImage.layer.borderWidth = 1
        userImage.layer.masksToBounds = false
        userImage.layer.borderColor = UIColor.white.cgColor
        userImage.layer.cornerRadius = userImage.frame.height/4
        userImage.clipsToBounds = true
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    override func setJoke(_ joke: Joke) {
        super.setJoke(joke)
        userName.text = joke.userName
    }
    
    func setCountry(_ language: String) {
        switch(language) {
        case "en":
            userCountry.image = UIImage(named:"country_en")
        case "ch":
            userCountry.image = UIImage(named:"country_ch")
        case "de":
            userCountry.image = UIImage(named:"country_de")
        case "fr":
            userCountry.image = UIImage(named:"country_fr")
        case "it":
            userCountry.image = UIImage(named:"country_it")
        case "es":
            userCountry.image = UIImage(named:"country_es")
        case "pt":
            userCountry.image = UIImage(named:"country_pt")
        default:
            //TODO: Set a default image
            userCountry.image = nil
            break
        }
    }
    
    @IBAction func favoriteJoke(_ sender: AnyObject){
        favoriteButton.isSelected = !favoriteButton.isSelected
        if self.jokeIndex != nil {delegate.setFavorites(self.jokeIndex!, isFavorite: favoriteButton.isSelected)}
    }
}

class StatusCell: UITableViewCell {
    
    @IBOutlet weak var countryImage: UIImageView!
    @IBOutlet weak var userImage: UIImageView!

    @IBOutlet weak var userName: UILabel!
    @IBOutlet weak var jokesCount: UILabel!
    @IBOutlet weak var following: UILabel!
    @IBOutlet weak var followers: UILabel!
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        userImage.layer.borderWidth = 1
        userImage.layer.masksToBounds = false
        userImage.layer.borderColor = UIColor.black.cgColor
        userImage.layer.cornerRadius = userImage.frame.height/2
        userImage.clipsToBounds = true
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    func setCountry(_ language: String) {
        switch(language) {
        case "en":
            countryImage.image = UIImage(named:"country_en")
        case "ch":
            countryImage.image = UIImage(named:"country_ch")
        case "de":
            countryImage.image = UIImage(named:"country_de")
        case "fr":
            countryImage.image = UIImage(named:"country_fr")
        case "it":
            countryImage.image = UIImage(named:"country_it")
        case "es":
            countryImage.image = UIImage(named:"country_es")
        case "pt":
            countryImage.image = UIImage(named:"country_pt")
        default:
            //TODO: Set a default image
            countryImage.image = nil
            break
        }
    }
}
