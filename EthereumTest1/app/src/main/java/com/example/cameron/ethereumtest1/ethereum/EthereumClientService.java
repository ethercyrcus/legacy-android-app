package com.example.cameron.ethereumtest1.ethereum;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.example.cameron.ethereumtest1.database.DBEthereumTransaction;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DBPublicationContentItem;
import com.example.cameron.ethereumtest1.database.DBUserContentItem;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.model.ContentItem;
import com.example.cameron.ethereumtest1.util.Convert;
import com.example.cameron.ethereumtest1.util.PrefUtils;
import com.google.gson.Gson;
import org.ethereum.geth.Account;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Block;
import org.ethereum.geth.BoundContract;
import org.ethereum.geth.CallOpts;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Hash;
import org.ethereum.geth.Header;
import org.ethereum.geth.Interface;
import org.ethereum.geth.Interfaces;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.Receipt;
import org.ethereum.geth.Signer;
import org.ethereum.geth.SyncProgress;
import org.ethereum.geth.TransactOpts;
import org.ethereum.geth.Transaction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import io.ipfs.kotlin.IPFS;
import io.ipfs.kotlin.commands.Get;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.ETH_DATA_DIRECTORY;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.KEY_STORE;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.PUBLICATION_REGISTER_ABI;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.RINKEBY_NETWORK_ID;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.USER_CONTENT_REGISTER_ABI;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.getRinkebyGenesis;

public class EthereumClientService extends Service {

    private final static String TAG = EthereumClientService.class.getName();

    private static final java.lang.String MESSAGE_ACTION = "message.action";

    public static final String START_ETHEREUM_SERVICE = "com.example.cameron.ethereumtest1.ethereum.action.start";

    public static final String UI_UPDATE_ETH_BLOCK = "ui.update.eth.block";
    public static final String PARAM_BLOCK_NUMBER = "param.block.number";

    public static final String ETH_FETCH_ACCOUNT_BALANCE = "eth.fetch.account.balance";
    public static final String PARAM_ADDRESS_STRING = "param.address.string";
    public static final String UI_UPDATE_ACCOUNT_BALANCE = "ui.update.account.balance";
    public static final String PARAM_BALANCE_WEI_STRING = "param.balance.wei.string";

    public static final String ETH_FETCH_ACCOUNT_USER_INFO = "eth.fetch.account.user.name";
    public static final String UI_UPDATE_ACCOUNT_USER_INFO = "ui.update.account.user.name";
    public static final String PARAM_USER_NAME = "param.user.name";
    public static final String PARAM_USER_ICON_URL = "param.user.icon.url";
    public static final String PARAM_PASSWORD = "param.password";

    public static final String ETH_FETCH_USER_CONTENT_LIST = "eth.fetch.user.content.list";
    public static final String UI_UPDATE_USER_CONTENT_LIST = "ui.update.user.content.list";
    public static final String PARAM_CONTENT_ITEM = "param.content.item";
    public static final String PARAM_ARRAY_CONTENT_STRING = "param.array.content.string";
    public static final String PARAM_ARRAY_CONTENT_REVENUE_STRING = "param.array.content.revenue.string";

    public static final String ETH_REGISTER_USER = "eth.register.user";
    public static final String UI_REGISTER_USER_PENDING_CONFIRMATION = "ui.register.user.pending.confirmation";

    public static final String ETH_UPDATE_USER_PIC = "eth.update.user.pic";
    public static final String UI_UPDATE_USER_PIC_PENDING_CONFIRMATION = "ui.update.user.pic.pending.confirmation";
    public static final String PARAM_USER_IMAGE_PATH = "param.user.image.path";


    public static final String ETH_PUBLISH_USER_CONTENT = "eth.publish.user.content";
    public static final String UI_PUBLISH_USER_CONTENT_PENDING_CONFIRMATION = "ui.publish.user.content.pending.confirmation";

    public static final String ETH_FETCH_PUBLICATION_CONTENT = "eth.fetch.publication.content";
    public static final String UI_UPDATE_PUBLICATION_CONTENT = "ui.update.publication.content";
    public static final String PARAM_PUBLICATION_INDEX = "param.publication.index";

    public static final String ETH_PUBLISH_USER_CONTENT_TO_PUBLICATION = "eth.publish.user.content.to.publication";
    public static final String UI_PUBLISH_USER_CONTENT_TO_PUBLICATION_PENDING_CONFIRMATION = "ui.publish.user.content.to.publication..pending.confirmation";
    public static final String PARAM_USER_CONTENT_INDEX = "param.user.content.index";
    public static final String PARAM_WHICH_PUBLICATION = "param.which.publication";

    public static final String IPFS_FETCH_DRAFT_IMAGE_URL = "eth.update.draft.photo.url";
    public static final String UI_UPDATE_DRAFT_PHOTO_URL = "ui.update.draft.photo.url";
    public static final String PARAM_DRAFT_PHOTO_URL = "param.draft.photo.url";

    public static final String ETH_SEND_ETH = "eth.send.eth";
    public static final String PARAM_RECIPIENT = "param.recipient";
    public static final String PARAM_AMOUNT = "param.amount";

    public static final String ETH_FETCH_NUM_PUBLICATIONS = "eth.fetch.num.publications";
    public static final String PARAM_NUM_PUBLICATIONS = "param.num.publications";

    public static final String ETH_FETCH_PUBLICATION_LIST = "eth.fetch.publication.list";
    public static final String UI_UPDATE_PUBLICATION_LIST = "ui.update.publication.list";

    public static final String ETH_CREATE_PUBLICATION = "eth.create.publication";
    public static final String PARAM_PUB_NAME = "param.pub.name";
    public static final String PARAM_PUB_META_DATA = "param.pub.meta.data";
    public static final String PARAM_PUB_MIN_COST_WEI = "param.pub.min.cost.wei";
    public static final String PARAM_PUB_ADMIN_PAY = "param.pub.admin.pay";
    private static final String UI_CREATE_PUBLICATION_PENDING_CONFIRMATION = "ui.create.publication.pending.confirmation";

    public static final String ETH_SUPPORT_POST = "eth.support.post";
    public static final String PARAM_COMMENT = "param.comment";
    public static final String PARAM_PUBLICATION_CONTENT_ITEM_NUMBER = "param.publication.content.item.number";
    private static final String UI_SUPPORT_POST_SENT = "ui.support.post.sent.pending.confirmation";

    public static final String ETH_FETCH_COMMENTS = "eth.fetch.comments";
    public static final String PARAM_NUM_COMMENTS = "param.num.comments";
    public static final String UI_UPDATE_PUBLICATION_CONTENT_COMMENTS_LIST = "ui.update.publication.content.comments.list";
    public static final String PARAM_ARRAY_PUBLICATION_CONTENT_COMMENTS_LIST = "param.array.publication.content.comments.list";

    public static final String ETH_FETCH_AUTHOR_CLAIM_AMOUNT = "eth.fetch.author.claim.amount";
    public static final String UI_UPDATE_AMOUNT_OWED_AUTHOR = "ui.update.amount.owed.author";
    public static final String PARAM_AMOUNT_OWED_AUTHOR = "param.amount.owed.author";

    public static final String ETH_WITHDRAW_AUTHOR_CLAIM = "eth.withdraw.author.claim";
    public static final String UI_WITHDRAW_AUTHOR_CLAIM_SENT = "ui.withdraw.author.claim.sent";

    public static final String ETH_WITHDRAW_ADMIN_CLAIM = "eth.withdraw.admin.claim";
    public static final String UI_WITHDRAW_ADMIN_CLAIM_SENT = "ui.withdraw.admin.claim.sent";
    public static final String PARAM_WITHDRAW_ADMIN_CLAIM_WHICH_PUB = "param.withdraw.admin.claim.which.pub";

    public static final String ETH_PERMISSION_AUTHOR = "eth.permission.author";
    public static final String UI_PERMISSION_AUTHOR_SENT = "ui.permission.author.sent";
    public static final String PARAM_PERMISSION_GRANTED_BOOL = "param.permission.granted.bool";
    public static final String PARAM_PERMISSION_WHICH_PUBLICATION = "param.permission.which.publication";
    public static final String PARAM_PERMISSION_WHICH_AUTHOR = "param.permission.which.author";

    public static final String ETH_UPDATE_PUBLICATION_IMAGE = "eth.update.publication.image";
    public static final String UI_UPDATE_PUBLICATION_IMAGE = "ui.update.publication.image";
    public static final String PARAM_PUBLICATION_IMAGE_FILE_PATH = "param.publication.image.file.path";
    public static final String PARAM_PUBLICATION_IMAGE_URL = "param.publication.image.file.url";

    public static final String RESTART_ETHEREUM_CLIENT = "restart.ethereum.client";
    public static final String PARAM_HIGHEST_BLOCK_NUMBER = "param.highest.block.number";


    private EthereumClient mEthereumClient;
    private org.ethereum.geth.Context mContext;
    private Node mNode;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private volatile boolean mIsReady = false;
    public long mBlockNumber = 0;
    public long mTimeOfLastUpdateMillis = 0;
    public long mHighestBlockNumberDuringSync = 0;
    public SyncProgress mSyncProgress;

    public EthereumClientService() {}

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            switch(b.getString(MESSAGE_ACTION)) {
                case START_ETHEREUM_SERVICE:
                    if (!mIsReady) {
                        handleStartEthereumService();
                    }
                    break;
                case ETH_FETCH_ACCOUNT_BALANCE:
                    String addressString = b.getString(PARAM_ADDRESS_STRING);
                    handleFetchAccountBalance(addressString);
                    break;
                case ETH_FETCH_ACCOUNT_USER_INFO:
                    addressString = b.getString(PARAM_ADDRESS_STRING);
                    handleFetchAccountUserInfo(addressString);
                    break;
                case ETH_FETCH_USER_CONTENT_LIST:
                    addressString = b.getString(PARAM_ADDRESS_STRING);
                    handleFetchUserContentList(addressString);
                    break;
                case ETH_REGISTER_USER:
                    String userName = b.getString(PARAM_USER_NAME);
                    String password = b.getString(PARAM_PASSWORD);
                    handleRegisterUser(userName, password);
                    break;
                case ETH_PUBLISH_USER_CONTENT:
                    ContentItem content = b.getParcelable(PARAM_CONTENT_ITEM);
                    password = b.getString(PARAM_PASSWORD);
                    handlePublishUserContent(content, password);
                    break;
                case ETH_FETCH_PUBLICATION_CONTENT:
                    int index = b.getInt(PARAM_PUBLICATION_INDEX);
                    handleFetchPublicationContent(index);
                    break;
                case ETH_PUBLISH_USER_CONTENT_TO_PUBLICATION:
                    int whichPub = b.getInt(PARAM_WHICH_PUBLICATION);
                    index = b.getInt(PARAM_USER_CONTENT_INDEX);
                    password = b.getString(PARAM_PASSWORD);
                    handlePublishUserContentToPublication(whichPub, index, password);
                    break;
                case ETH_UPDATE_USER_PIC:
                    String picPath = b.getString(PARAM_USER_IMAGE_PATH);
                    password = b.getString(PARAM_PASSWORD);
                    handleUpdateUserPic(picPath, password);
                    break;
                case IPFS_FETCH_DRAFT_IMAGE_URL:
                    String draftImagePath = b.getString(PARAM_DRAFT_PHOTO_URL);
                    handleFetchDraftImageURL(draftImagePath);
                    break;
                case ETH_FETCH_PUBLICATION_LIST:
                    handleFetchPublicationList();
                    break;
                case ETH_SEND_ETH:
                    String recipient = b.getString(PARAM_RECIPIENT);
                    String amount = b.getString(PARAM_AMOUNT);
                    password = b.getString(PARAM_PASSWORD);
                    handleSendEth(recipient, amount, password);
                    break;
                case ETH_CREATE_PUBLICATION:
                    String name = b.getString(PARAM_PUB_NAME);
                    String meta = b.getString(PARAM_PUB_META_DATA);
                    String minCost = b.getString(PARAM_PUB_MIN_COST_WEI);
                    String adminPay = b.getString(PARAM_PUB_ADMIN_PAY);
                    password = b.getString(PARAM_PASSWORD);
                    handleCreatePublication(name, meta, minCost, adminPay, password);
                    break;
                case ETH_SUPPORT_POST:
                    String supportAmount = b.getString(PARAM_AMOUNT);
                    String comment = b.getString(PARAM_COMMENT);
                    int whichPublication = b.getInt(PARAM_WHICH_PUBLICATION);
                    int contentIndex = b.getInt(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER);
                    password = b.getString(PARAM_PASSWORD);
                    handleSupportPost(supportAmount, comment, whichPublication, contentIndex, password);
                    break;
                case ETH_FETCH_COMMENTS:
                    whichPublication = b.getInt(PARAM_WHICH_PUBLICATION);
                    int publicationContentItemNumber = b.getInt(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER);
                    int numComments = b.getInt(PARAM_NUM_COMMENTS);
                    handleFetchComments(whichPublication, publicationContentItemNumber, numComments);
                    break;
                case ETH_FETCH_AUTHOR_CLAIM_AMOUNT:
                    whichPublication = b.getInt(PARAM_WHICH_PUBLICATION);
                    String selectedUser = b.getString(PARAM_ADDRESS_STRING);
                    handleFetchAuthorClaimAmount(whichPublication, selectedUser);
                    break;
                case ETH_WITHDRAW_AUTHOR_CLAIM:
                    whichPublication = b.getInt(PARAM_WHICH_PUBLICATION);
                    String selectedUserForWithdraw = b.getString(PARAM_ADDRESS_STRING);
                    password = b.getString(PARAM_PASSWORD);
                    handleWithdrawAuthorClaim(whichPublication, selectedUserForWithdraw, password);
                    break;
                case ETH_WITHDRAW_ADMIN_CLAIM:
                    whichPublication = b.getInt(PARAM_WITHDRAW_ADMIN_CLAIM_WHICH_PUB);
                    password = b.getString(PARAM_PASSWORD);
                    handleWithdrawAdminClaim(whichPublication, password);
                    break;
                case ETH_PERMISSION_AUTHOR:
                    whichPublication = b.getInt(PARAM_PERMISSION_WHICH_PUBLICATION);
                    String whichAuthor = b.getString(PARAM_PERMISSION_WHICH_AUTHOR);
                    boolean permissionEnabled = b.getBoolean(PARAM_PERMISSION_GRANTED_BOOL);
                    password = b.getString(PARAM_PASSWORD);
                    handlePermissionAuthor(whichPublication, whichAuthor, permissionEnabled, password);
                    break;
                case RESTART_ETHEREUM_CLIENT:
                    handleRestartEthereumClient();
                default:
                    break;
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        Bundle b = new Bundle();
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ETH_FETCH_ACCOUNT_BALANCE:
                case ETH_FETCH_ACCOUNT_USER_INFO:
                case ETH_FETCH_USER_CONTENT_LIST:
                    b.putString(PARAM_ADDRESS_STRING, intent.getStringExtra(PARAM_ADDRESS_STRING));
                    break;
                case ETH_REGISTER_USER:
                    b.putString(PARAM_USER_NAME, intent.getStringExtra(PARAM_USER_NAME));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_PUBLISH_USER_CONTENT:
                    b.putParcelable(PARAM_CONTENT_ITEM, intent.getParcelableExtra(PARAM_CONTENT_ITEM));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_FETCH_PUBLICATION_CONTENT:
                    b.putInt(PARAM_PUBLICATION_INDEX, intent.getIntExtra(PARAM_PUBLICATION_INDEX, 0));
                    break;
                case ETH_PUBLISH_USER_CONTENT_TO_PUBLICATION:
                    b.putInt(PARAM_WHICH_PUBLICATION, intent.getIntExtra(PARAM_WHICH_PUBLICATION, 0));
                    b.putInt(PARAM_USER_CONTENT_INDEX, intent.getIntExtra(PARAM_USER_CONTENT_INDEX, 0));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_UPDATE_USER_PIC:
                    b.putString(PARAM_USER_IMAGE_PATH, intent.getStringExtra(PARAM_USER_IMAGE_PATH));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case IPFS_FETCH_DRAFT_IMAGE_URL:
                    b.putString(PARAM_DRAFT_PHOTO_URL, intent.getStringExtra(PARAM_DRAFT_PHOTO_URL));
                    break;
                case ETH_SEND_ETH:
                    b.putString(PARAM_RECIPIENT, intent.getStringExtra(PARAM_RECIPIENT));
                    b.putString(PARAM_AMOUNT, intent.getStringExtra(PARAM_AMOUNT));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_CREATE_PUBLICATION:
                    b.putString(PARAM_PUB_NAME, intent.getStringExtra(PARAM_PUB_NAME));
                    b.putString(PARAM_PUB_META_DATA, intent.getStringExtra(PARAM_PUB_META_DATA));
                    b.putString(PARAM_PUB_MIN_COST_WEI, intent.getStringExtra(PARAM_PUB_MIN_COST_WEI));
                    b.putString(PARAM_PUB_ADMIN_PAY, intent.getStringExtra(PARAM_PUB_ADMIN_PAY));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_SUPPORT_POST:
                    b.putString(PARAM_AMOUNT, intent.getStringExtra(PARAM_AMOUNT));
                    b.putString(PARAM_COMMENT, intent.getStringExtra(PARAM_COMMENT));
                    b.putInt(PARAM_WHICH_PUBLICATION, intent.getIntExtra(PARAM_WHICH_PUBLICATION, -1));
                    b.putInt(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER, intent.getIntExtra(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER, -1));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_FETCH_COMMENTS:
                    b.putInt(PARAM_WHICH_PUBLICATION, intent.getIntExtra(PARAM_WHICH_PUBLICATION, -1));
                    b.putInt(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER, intent.getIntExtra(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER, -1));
                    b.putInt(PARAM_NUM_COMMENTS, intent.getIntExtra(PARAM_NUM_COMMENTS, -1));
                    break;
                case ETH_FETCH_AUTHOR_CLAIM_AMOUNT:
                    b.putInt(PARAM_WHICH_PUBLICATION, intent.getIntExtra(PARAM_WHICH_PUBLICATION, -1));
                    b.putString(PARAM_ADDRESS_STRING, intent.getStringExtra(PARAM_ADDRESS_STRING));
                    break;
                case ETH_WITHDRAW_AUTHOR_CLAIM:
                    b.putInt(PARAM_WHICH_PUBLICATION, intent.getIntExtra(PARAM_WHICH_PUBLICATION, -1));
                    b.putString(PARAM_ADDRESS_STRING, intent.getStringExtra(PARAM_ADDRESS_STRING));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_WITHDRAW_ADMIN_CLAIM:
                    b.putInt(PARAM_WITHDRAW_ADMIN_CLAIM_WHICH_PUB, intent.getIntExtra(PARAM_WITHDRAW_ADMIN_CLAIM_WHICH_PUB, -1));
                    b.putString(PARAM_ADDRESS_STRING, intent.getStringExtra(PARAM_ADDRESS_STRING));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
                case ETH_PERMISSION_AUTHOR:
                    b.putInt(PARAM_PERMISSION_WHICH_PUBLICATION, intent.getIntExtra(PARAM_PERMISSION_WHICH_PUBLICATION, -1));
                    b.putString(PARAM_PERMISSION_WHICH_AUTHOR, intent.getStringExtra(PARAM_PERMISSION_WHICH_AUTHOR));
                    b.putBoolean(PARAM_PERMISSION_GRANTED_BOOL, intent.getBooleanExtra(PARAM_PERMISSION_GRANTED_BOOL, false));
                    b.putString(PARAM_PASSWORD, intent.getStringExtra(PARAM_PASSWORD));
                    break;
            }
            String s = intent.getAction();
            b.putString(MESSAGE_ACTION, s);
            msg.setData(b);
            mServiceHandler.sendMessage(msg);
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        try {
            mNode.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void handleRestartEthereumClient() {
        try {
            //mNode.stop();
            mEthereumClient.subscribeNewHead(mContext, mNewHeadHandler, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //stopSelf();
    }

    private void handleStartEthereumService() {
        mContext = new org.ethereum.geth.Context();

        final NodeConfig config = new NodeConfig();
        config.setEthereumEnabled(true);
        config.setBootstrapNodes(EthereumConstants.getRinkebyBootNodes());
        config.setEthereumNetworkID(RINKEBY_NETWORK_ID);
        config.setEthereumGenesis(getRinkebyGenesis(getBaseContext()));


        try {
            File ethDataDirectory = new File(getFilesDir() + ETH_DATA_DIRECTORY + "/GethDroid");
            if (!ethDataDirectory.exists()) ethDataDirectory.mkdirs();
            File staticNodes = new File(getFilesDir() + ETH_DATA_DIRECTORY + "/GethDroid", "static-nodes.json");
            if (!staticNodes.exists()) staticNodes.createNewFile();
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(new FileOutputStream(staticNodes));
            String string = "[";
            for (String s: EthereumConstants.LIGHT_SERVERS) { //fuck you string immutability interview questions
                string = string + ("  \"" + s + "\",");
            }
            string = string + ("  \"" + EthereumConstants.LIGHT_SERV_PEER_NODE_ENODE_ADDRESS_4 + "\",");
            string = string + ("  \"" + EthereumConstants.LIGHT_SERV_PEER_NODE_ENODE_ADDRESS_METABARON + "\"]");
            outputStreamWriter.append(string);
            outputStreamWriter.close();

            if (mNode == null) {
                mNode = Geth.newNode(getFilesDir() + ETH_DATA_DIRECTORY, config);
            }
            mNode.start();
            mEthereumClient = mNode.getEthereumClient();
            Log.e("PEER", "Node Info ENODE: " + mNode.getNodeInfo().getEnode());
            Log.e("PEER", "Node Info IP: " + mNode.getNodeInfo().getIP());
            Log.e("PEER", "Node Info Listener Port: " + mNode.getNodeInfo().getListenerPort());
            Log.e("PEER", "Send to server: " + mNode.getNodeInfo().getEnode());
            sendAddTrustedNodeRequest();
            mEthereumClient.subscribeNewHead(mContext, mNewHeadHandler, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAddTrustedNodeRequest() {
        String ipAddress = "0.0.0.0";
        String inputLine;
        try {
            //Get my ip address since Geth is fucked up
            URL checkIPAddressURL = new URL("https://api.ipify.org?format=text");
            HttpURLConnection connection =(HttpURLConnection)checkIPAddressURL.openConnection();

            //Set methods and timeouts
            //connection.setRequestMethod(REQUEST_METHOD);
            //connection.setReadTimeout(READ_TIMEOUT);
            //connection.setConnectTimeout(CONNECTION_TIMEOUT);

            connection.connect();
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            ipAddress = stringBuilder.toString();


            URL myUrl = new URL("http://138.68.255.31:8080/" + "enode://" + mNode.getNodeInfo().getEnode().substring(8, 136) + "@" + ipAddress + ":" + mNode.getNodeInfo().getListenerPort());
            connection =(HttpURLConnection)myUrl.openConnection();
            connection.connect();
            streamReader = new
                    InputStreamReader(connection.getInputStream());
            reader = new BufferedReader(streamReader);
            stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            String response  = stringBuilder.toString();

            //Connect to our url
            myUrl = new URL("http://46.101.140.109:8080/" + "enode://" + mNode.getNodeInfo().getEnode().substring(8, 136) + "@" + ipAddress + ":" + mNode.getNodeInfo().getListenerPort());
            connection =(HttpURLConnection)myUrl.openConnection();
            connection.connect();
            streamReader = new
                    InputStreamReader(connection.getInputStream());
            reader = new BufferedReader(streamReader);
            stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            response  = stringBuilder.toString();

        } catch (Exception e) {
            Log.e("AddTrustedNode", e.getMessage());

        }
    }

    private NewHeadHandler mNewHeadHandler = new NewHeadHandler() {
        @Override
        public void onError(String error) {
        }

        @Override
        public void onNewHead(final Header header) {
            if (mHighestBlockNumberDuringSync == 0) {
                try {
                    mSyncProgress = mEthereumClient.syncProgress(mContext);
                    mHighestBlockNumberDuringSync = mSyncProgress.getHighestBlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - mTimeOfLastUpdateMillis > 5000) {
                mBlockNumber = header.getNumber();
                Intent intent = new Intent(UI_UPDATE_ETH_BLOCK);
                intent.putExtra(PARAM_BLOCK_NUMBER, mBlockNumber);
                intent.putExtra(PARAM_HIGHEST_BLOCK_NUMBER, mHighestBlockNumberDuringSync);
                LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
                bm.sendBroadcast(intent);
                mTimeOfLastUpdateMillis = System.currentTimeMillis();
                mIsReady = true;

                try {
                    Log.e("PEER", "Node Info IP: " + mNode.getNodeInfo().getIP());
                    Log.e("PEER", "Node Info Listener Address: " + mNode.getNodeInfo().getListenerAddress());
                    Log.e("PEER", "Node Info Discovery Port: " + mNode.getNodeInfo().getDiscoveryPort());
                    Log.e("PEER", "Node Info Listener Port: " + mNode.getNodeInfo().getListenerPort());
                    Log.e("PEER", "Num Peers: " + mNode.getPeersInfo().size());
                    for (int i = 0; i < mNode.getPeersInfo().size(); i++) {
                        Log.e("PEER", "Peer " + i + ": " + mNode.getPeersInfo().get(i).getID());
                        Log.e("PEER", "Peer " + i + ": " + mNode.getPeersInfo().get(i).getRemoteAddress());
                        Log.e("PEER", "Peer " + i + ": " + mNode.getPeersInfo().get(i).getName());
                        //Log.e("PEER", "Peer " + i + ": " + mNode.getPeersInfo().get(i).getLocalAddress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void handleFetchAccountBalance(String addressString) {
        BigInt balance = new BigInt(0);
        boolean successful = false;

        try {
            balance = mEthereumClient.getBalanceAt(mContext, new Address(addressString), -1);
            successful = true;
        } catch (Exception e) {
            successful = false;
            Log.e(TAG, "Error retrieving balance: " + e.getMessage());
        }

        if (successful) {
            PrefUtils.saveSelectedAccountBalance(getBaseContext(), balance.getString(10));
            Log.e(TAG, "SUCCESSFULLY UPDATED BALANCE");

            Intent intent = new Intent(UI_UPDATE_ACCOUNT_BALANCE);
            intent.putExtra(PARAM_BALANCE_WEI_STRING, balance.getString(10));
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);
        }
    }

    private void handleFetchUserContentList(String addressString) {
        String contentString = "";
        int targetFetch = 5;

        try {
            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.USER_CONTENT_REGISTER_ADDRESS_RINKEBY),
                    USER_CONTENT_REGISTER_ABI, mEthereumClient);

            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;
            Interfaces returnData;

            //Find number of articles
            callData = Geth.newInterfaces(1);
            Interface address = Geth.newInterface();
            address.setAddress(Geth.newAddressFromHex(addressString));
            callData.set(0, address);

            returnData = Geth.newInterfaces(3);
            Interface userNameParam = Geth.newInterface();
            userNameParam.setDefaultString();
            Interface metaParam = Geth.newInterface();
            metaParam.setDefaultString();
            Interface numContentParam = Geth.newInterface();
            numContentParam.setDefaultBigInt();
            returnData.set(0, userNameParam);
            returnData.set(1, metaParam);
            returnData.set(2, numContentParam);

            contract.call(callOpts, returnData, "userIndex", callData);
            long numContent = returnData.get(2).getBigInt().getInt64();

            ArrayList<String> postJsonArray = new ArrayList<>();
            ArrayList<DBUserContentItem> dbSaveList = new ArrayList<>();

            long howFarBack = numContent > targetFetch ? numContent - targetFetch : 0;

            for (long i = numContent - 1; i >= 0 && i >= howFarBack; i--) {
                callData = Geth.newInterfaces(2);
                Interface index = Geth.newInterface();
                index.setBigInt(Geth.newBigInt(i));
                callData.set(0, address);
                callData.set(1, index);

                returnData = Geth.newInterfaces(1);
                Interface content = Geth.newInterface();
                content.setDefaultString();
                returnData.set(0, content);

                contentString = contract.callForString(callOpts, "getUserContent", callData);

                String json = "";
                try {
                    Get get = new IPFS().getGet();
                    json = new IPFS().getGet().cat(contentString);

                } catch (Exception e) {
                    json = "CONTENT CURRENTLY UNAVAILABLE";
                    Log.e("UserContent Unavailable", contentString + " : " +e.getMessage());
                    continue;
                }

                postJsonArray.add(json);
                ContentItem ci = convertJsonToContentItem(json);

                DBUserContentItem dbuci = new DBUserContentItem(addressString, (int)i, contentString, ci.primaryImageUrl,
                        json, ci.title, ci.primaryText, ci.publishedDate, true, false);
                dbSaveList.add(dbuci);
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File ethercircusFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ethercircus");
                    if (!ethercircusFolder.exists()) ethercircusFolder.mkdirs();
                    File addressFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + "ethercircus", addressString);
                    if (!addressFolder.exists()) addressFolder.mkdirs();
                    File contentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/ethercircus/" + addressString + "/", "content_" + i);
                    if (!contentFolder.exists()) contentFolder.mkdirs();
                    FileOutputStream fo = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "ethercircus" + File.separator + addressString + File.separator + "content_" + i + File.separator + "content.json");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
                    outputStreamWriter.write(json);
                    outputStreamWriter.close();
                    fo.close();
                }
            }

            DatabaseHelper db = new DatabaseHelper(getBaseContext());
            db.saveUserContentItems(dbSaveList);

            Intent intent = new Intent(UI_UPDATE_USER_CONTENT_LIST);
            intent.putStringArrayListExtra(PARAM_ARRAY_CONTENT_STRING, postJsonArray);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error retrieving contentList: " + e.getMessage());
        }

    }

    private void handleFetchPublicationContent(int index) {
        String contentString = "";

        try {
            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;
            Interfaces returnData;

            //Find number of content items
            callData = Geth.newInterfaces(1);
            Interface publicationIndex = Geth.newInterface();
            publicationIndex.setBigInt(new BigInt(index));
            callData.set(0, publicationIndex);

            returnData = Geth.newInterfaces(1);
            Interface numPublishedParam = Geth.newInterface();
            numPublishedParam.setDefaultBigInt();
            returnData.set(0, numPublishedParam);

            contract.callFix(callOpts, returnData, "getNumPublished", callData);
            long numPublished = returnData.get(0).getBigInt().getInt64();

            ////////////////////////////////
            BoundContract contract2 = Geth.bindContract(
                    new Address(EthereumConstants.USER_CONTENT_REGISTER_ADDRESS_RINKEBY),
                    USER_CONTENT_REGISTER_ABI, mEthereumClient);
            CallOpts callOpts2 = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData2;
            Interfaces returnData2;

            callData2 = Geth.newInterfaces(1);
            Interface paramFetchUsernameCallParameter2 = Geth.newInterface();

            //////////////////////////////////

//            long targetFetch = 5;
//            long howFarBack = numPublished > targetFetch ? numPublished - targetFetch : 0;
//            for (long i = numContent - 1; i >= 0 && i >= howFarBack; i--) {

            ArrayList<String> postJsonArray = new ArrayList<>();
            ArrayList<Integer> postUniqueSupportersArray = new ArrayList<>();
            ArrayList<String> postRevenue = new ArrayList<>();
            ArrayList<DBPublicationContentItem> dbSaveList = new ArrayList<>();
            int counter = 0;
            for (long i = numPublished - 1; i >= 0 && counter < 5; i--) {
                callData = Geth.newInterfaces(2);
                callData.set(0, publicationIndex);
                Interface contentIndex = Geth.newInterface();
                contentIndex.setBigInt(Geth.newBigInt(i));
                callData.set(1, contentIndex);

                returnData = Geth.newInterfaces(1);
                Interface content = Geth.newInterface();
                content.setDefaultString();
                returnData.set(0, content);

                contentString = contract.callForString(callOpts, "getContent", callData);

                Interface revenue = Geth.newInterface();
                revenue.setDefaultBigInt();
                returnData.set(0, revenue);

                contract.callFix(callOpts, returnData, "getContentRevenue", callData);
                String contentRevenue = returnData.get(0).getBigInt().getString(10);
                postRevenue.add(contentRevenue);

                Interface uniqueSupporters = Geth.newInterface();
                uniqueSupporters.setDefaultBigInt();
                returnData.set(0, uniqueSupporters);

                contract.callFix(callOpts, returnData, "getContentUniqueSupporters", callData);
                long contentUniqueSupporters = returnData.get(0).getBigInt().getInt64();

                Interface numComments = Geth.newInterface();
                numComments.setDefaultBigInt();
                returnData.set(0, numComments);

                contract.callFix(callOpts, returnData, "getContentNumComments", callData);
                long contentNumComments = returnData.get(0).getBigInt().getInt64();


                String json = "";
                try {
                    json = new IPFS().getGet().cat(contentString);
                } catch (Exception e) {
                    json = "CONTENT CURRENTLY UNAVAILABLE";// + e.getMessage();
                    Log.e("Pub Content Unavailable", contentString + " : " +e.getMessage());
                    continue;
                }
                ContentItem ci = convertJsonToContentItem(json);
                if (ci == null) continue;
                counter++;
                paramFetchUsernameCallParameter2.setAddress(new Address(ci.publishedBy));
                callData2.set(0, paramFetchUsernameCallParameter2);

                returnData2 = Geth.newInterfaces(3);
                Interface userNameData2 = Geth.newInterface();
                Interface metaData2 = Geth.newInterface();
                Interface numContent2 = Geth.newInterface();
                userNameData2.setDefaultString();
                metaData2.setDefaultString();
                numContent2.setDefaultBigInt();
                returnData2.set(0, userNameData2);
                returnData2.set(1, metaData2);
                returnData2.set(2, numContent2);

                contract2.call(callOpts2, returnData2, "userIndex", callData2);
                String userName = returnData2.get(0).getString();
                String metadataIconUrl = returnData2.get(1).getString();

                ci.publishedBy = userName;
                postJsonArray.add(convertContentItemToJSON(ci));

                DBPublicationContentItem dbpci = new DBPublicationContentItem(index, (int)i, ci.publishedBy, contentString, ci.primaryImageUrl, json, ci.title, ci.primaryText, ci.publishedDate, contentUniqueSupporters, contentRevenue, (int)contentNumComments);
                dbSaveList.add(dbpci);

            }

            DatabaseHelper db = new DatabaseHelper(getApplicationContext());
            db.savePublicationContentItems(dbSaveList);
            Intent intent = new Intent(UI_UPDATE_PUBLICATION_CONTENT);
            intent.putStringArrayListExtra(PARAM_ARRAY_CONTENT_STRING, postJsonArray);
            intent.putStringArrayListExtra(PARAM_ARRAY_CONTENT_REVENUE_STRING, postRevenue);
            intent.putExtra("whichPub", index);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error retrieving contentList: " + e.getMessage());
        }
    }

    private void handleFetchDraftImageURL(String draftImagePath) {
        File f = new File(draftImagePath);
        File smallerFile = saveBitmapToFile(f);
        final String contentHash = new IPFS().getAdd().file(smallerFile).getHash();
        Intent intent = new Intent(UI_UPDATE_DRAFT_PHOTO_URL);
        intent.putExtra(PARAM_DRAFT_PHOTO_URL, contentHash);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
    }

    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=25;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void handleFetchAccountUserInfo(String addressString) {
        String userName = "";

        try {
            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.USER_CONTENT_REGISTER_ADDRESS_RINKEBY),
                    USER_CONTENT_REGISTER_ABI, mEthereumClient);
            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;
            Interfaces returnData;

            callData = Geth.newInterfaces(1);
            Interface paramFetchUsernameCallParameter = Geth.newInterface();
            paramFetchUsernameCallParameter.setAddress(new Address(addressString));
            callData.set(0, paramFetchUsernameCallParameter);

            returnData = Geth.newInterfaces(3);
            Interface userNameData = Geth.newInterface();
            Interface metaData = Geth.newInterface();
            Interface numContent = Geth.newInterface();
            userNameData.setDefaultString();
            metaData.setDefaultString();
            numContent.setDefaultBigInt();
            returnData.set(0, userNameData);
            returnData.set(1, metaData);
            returnData.set(2, numContent);

            contract.call(callOpts, returnData, "userIndex", callData);
            userName = returnData.get(0).getString();
            String metadataIconUrl = returnData.get(1).getString();

            if (!TextUtils.isEmpty(userName)) {
                Log.e(TAG, "SUCCESSFULLY UPDATED USER NAME");
            } else {
                userName = "not yet registered...";
                Log.e(TAG, "NO USER NAME FOUND FOR " + addressString);
            }
            PrefUtils.saveSelectedAccountUserName(getBaseContext(), userName);

            Intent intent = new Intent(UI_UPDATE_ACCOUNT_USER_INFO);
            intent.putExtra(PARAM_USER_NAME, userName);
            intent.putExtra(PARAM_USER_ICON_URL, metadataIconUrl);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving username: " + e.getMessage());
        }
    }

    private void handleFetchPublicationList() {
        ArrayList<DBPublication> dbSaveList = new ArrayList<>();
        try {
            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;
            Interfaces returnData;

            //Find number of Publications
            callData = Geth.newInterfaces(0);

            returnData = Geth.newInterfaces(1);
            Interface numPublicationsParam = Geth.newInterface();
            numPublicationsParam.setDefaultBigInt();
            returnData.set(0, numPublicationsParam);

            contract.callFix(callOpts, returnData, "numPublications", callData);
            long numPublications = returnData.get(0).getBigInt().getInt64();

            ////////////////////////////////

            Interfaces callData2;
            Interfaces returnData2;

            callData2 = Geth.newInterfaces(1);
            Interface paramWhichPublication = Geth.newInterface();

            //////////////////////////////////

            int counter = 0;
            for (long i = numPublications - 1; i >= 7 && counter <= 10; i--) {
                paramWhichPublication.setBigInt(new BigInt(i));
                callData2.set(0, paramWhichPublication);
                returnData2 = Geth.newInterfaces(9);

                Interface name = Geth.newInterface();
                Interface metadata = Geth.newInterface();
                Interface adminAddress = Geth.newInterface();
                Interface open = Geth.newInterface();
                Interface numPublished = Geth.newInterface();
                Interface minCost = Geth.newInterface();
                Interface adminPercentage = Geth.newInterface();
                Interface supporters = Geth.newInterface();
                Interface adminClaimsOwed = Geth.newInterface();

                name.setDefaultString();
                metadata.setDefaultString();
                adminAddress.setDefaultAddress();
                open.setDefaultBool();

                numPublished.setDefaultBigInt();
                minCost.setDefaultBigInt();
                adminPercentage.setDefaultUint8();
                supporters.setDefaultBigInt();
                adminClaimsOwed.setDefaultBigInt();

                returnData2.set(0, name);
                returnData2.set(1, metadata);
                returnData2.set(2, adminAddress);
                returnData2.set(3, open);
                returnData2.set(4, numPublished);
                returnData2.set(5, minCost);
                returnData2.set(6, adminPercentage);
                returnData2.set(7, supporters);
                returnData2.set(8, adminClaimsOwed);

                contract.call(callOpts, returnData2, "publicationIndex", callData2);

                String nameString = returnData2.get(0).getString();
                String metaDataString = returnData2.get(1).getString();
                String adminString = returnData2.get(2).getAddress().getHex();
                long numPublishedLong = returnData2.get(4).getBigInt().getInt64();
                long numSupporters = returnData2.get(7).getBigInt().getInt64();
                long adminClaimsOwedLong = returnData2.get(8).getBigInt().getInt64();

                DBPublication dbPub = new DBPublication((int)i, nameString, metaDataString, adminString, (int)numPublishedLong, 0, 0, (int)numSupporters, false, adminClaimsOwedLong);
                dbSaveList.add(dbPub);
            }

            paramWhichPublication.setBigInt(new BigInt(0));
            callData2.set(0, paramWhichPublication);
            returnData2 = Geth.newInterfaces(9);

            Interface name = Geth.newInterface();
            Interface metadata = Geth.newInterface();
            Interface adminAddress = Geth.newInterface();
            Interface open = Geth.newInterface();
            Interface numPublished = Geth.newInterface();
            Interface minCost = Geth.newInterface();
            Interface adminPercentage = Geth.newInterface();
            Interface supporters = Geth.newInterface();
            Interface adminClaimsOwed = Geth.newInterface();

            name.setDefaultString();
            metadata.setDefaultString();
            adminAddress.setDefaultAddress();
            open.setDefaultBool();

            numPublished.setDefaultBigInt();
            minCost.setDefaultBigInt();
            adminPercentage.setDefaultUint8();
            supporters.setDefaultBigInt();
            adminClaimsOwed.setDefaultBigInt();

            returnData2.set(0, name);
            returnData2.set(1, metadata);
            returnData2.set(2, adminAddress);
            returnData2.set(3, open);
            returnData2.set(4, numPublished);
            returnData2.set(5, minCost);
            returnData2.set(6, adminPercentage);
            returnData2.set(7, supporters);
            returnData2.set(8, adminClaimsOwed);

            contract.call(callOpts, returnData2, "publicationIndex", callData2);

            String nameString = returnData2.get(0).getString();
            String metaDataString = returnData2.get(1).getString();
            String adminString = returnData2.get(2).getAddress().getHex();
            long numPublishedLong = returnData2.get(4).getBigInt().getInt64();
            long numSupporters = returnData2.get(7).getBigInt().getInt64();
            long adminClaimsOwedLong = returnData2.get(8).getBigInt().getInt64();

            DBPublication dbPub = new DBPublication(0, nameString, metaDataString, adminString, (int)numPublishedLong, 0, 0, (int)numSupporters, false, adminClaimsOwedLong);
            dbSaveList.add(dbPub);

            DatabaseHelper db = new DatabaseHelper(getApplicationContext());
            db.savePublications(dbSaveList);
            Intent intent = new Intent(UI_UPDATE_PUBLICATION_LIST);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error retrieving publication list: " + e.getMessage());
            if (dbSaveList.size() > 0) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.savePublications(dbSaveList);
                Intent intent = new Intent(UI_UPDATE_PUBLICATION_LIST);
                LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
                bm.sendBroadcast(intent);
            }
        }
    }

    private void handleFetchComments(int whichPublication, int publicationContentItemNumber, int numComments) {
        String contentString = "";

        try {
            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;

            callData = Geth.newInterfaces(3);
            Interface paramWhichPublication = Geth.newInterface();
            Interface paramContentIndex = Geth.newInterface();
            Interface paramCommentIndex = Geth.newInterface();

            paramWhichPublication.setBigInt(new BigInt(whichPublication));
            paramContentIndex.setBigInt(new BigInt(publicationContentItemNumber));

            callData.set(0, paramWhichPublication);
            callData.set(1, paramContentIndex);

            //////////////////////////////////

            ArrayList<String> commentsList = new ArrayList<>();
            int counter = 0;
            for (long i = numComments - 1; i >= 0 && counter <= 15; i--) {
                paramCommentIndex.setBigInt(new BigInt(i));
                callData.set(2, paramCommentIndex);
                String commentString = contract.callForString(callOpts, "getContentCommentByIndex", callData);
                commentsList.add(commentString);
            }

//            DatabaseHelper db = new DatabaseHelper(getBaseContext());
//            db.saveComments();

            Intent intent = new Intent(UI_UPDATE_PUBLICATION_CONTENT_COMMENTS_LIST);
            intent.putStringArrayListExtra(PARAM_ARRAY_PUBLICATION_CONTENT_COMMENTS_LIST, commentsList);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error retrieving contentList: " + e.getMessage());
        }
    }

    private void handleFetchAuthorClaimAmount(int whichPublication, String selectedUser) {
        String amountOwedString = "";

        try {
            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);
            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;
            Interfaces returnData;

            callData = Geth.newInterfaces(2);
            Interface paramWhichPublicationCallParameter = Geth.newInterface();
            paramWhichPublicationCallParameter.setBigInt(new BigInt(whichPublication));
            callData.set(0, paramWhichPublicationCallParameter);
            Interface paramSelectedUser = Geth.newInterface();
            paramSelectedUser.setAddress(new Address(selectedUser));
            callData.set(1, paramSelectedUser);

            returnData = Geth.newInterfaces(1);
            Interface amountOwedWei = Geth.newInterface();
            amountOwedWei.setDefaultBigInt();
            returnData.set(0, amountOwedWei);

            contract.call(callOpts, returnData, "checkAuthorClaim", callData);
            amountOwedString = returnData.get(0).getBigInt().toString();

            Intent intent = new Intent(UI_UPDATE_AMOUNT_OWED_AUTHOR);
            intent.putExtra(PARAM_AMOUNT_OWED_AUTHOR, amountOwedString);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
            bm.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching author claim: " + e.getMessage());
        }
    }

    /*
     *  Methods that send Transactions to the Ethereum Blockckain
     */

    private void handleSendEth(String recipient, String amountString, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            Address from = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));


            long nonce = mEthereumClient.getPendingNonceAt(mContext, from);
            Address to = new Address(recipient);
            BigInt amount = new BigInt(Convert.toWei(amountString, Convert.Unit.ETHER).longValue());
            long gasLimit = 100000l;
            BigInt gasPrice = new BigInt(Convert.toWei("40", Convert.Unit.GWEI).longValue());
            byte[] data = new byte[0];

            Transaction txSendEth = Geth.newTransaction(nonce, to, amount, gasLimit , gasPrice, data);
            Signer signer = new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    String from = account.getAddress().getHex();
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            };
            Transaction signedTransaction = signer.sign(from, txSendEth);
            transactionHash = signedTransaction.getHash();
            ethereumTransaction = new DBEthereumTransaction(from.getHex(), transactionHash.getHex(), DatabaseHelper.TX_ACTION_ID_SEND_ETH,
                    recipient + ":" + amountString, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, signedTransaction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handleRegisterUser(String userName, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.USER_CONTENT_REGISTER_ADDRESS_RINKEBY),
                    USER_CONTENT_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            tOpts.setValue(new BigInt(0));
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);
            Interfaces callParams = Geth.newInterfaces(2);
            Interface paramUsername = Geth.newInterface();
            paramUsername.setString(userName);
            callParams.set(0, paramUsername);
            Interface paramMetaData = Geth.newInterface();
            paramMetaData.setString("meta");
            callParams.set(1, paramMetaData);
            final Transaction txRegisterUser = contract.transact(tOpts, "registerNewUser", callParams);
            transactionHash = txRegisterUser.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_REGISTER, userName, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txRegisterUser);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_REGISTER_USER_PENDING_CONFIRMATION);
        intent.putExtra(PARAM_USER_NAME, userName);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handleUpdateUserPic(final String picPath, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract contract = Geth.bindContract(
                    new Address(EthereumConstants.USER_CONTENT_REGISTER_ADDRESS_RINKEBY),
                    USER_CONTENT_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            BigInt gasPrice = new BigInt(Convert.toWei("40", Convert.Unit.GWEI).longValue());
            tOpts.setGasPrice(gasPrice);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            tOpts.setValue(new BigInt(0));
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            //add file to IPFS
            File f = new File(picPath);
            final String contentHash = new IPFS().getAdd().file(f).getHash();


            Interfaces callParams = Geth.newInterfaces(1);
            Interface paramMetaData = Geth.newInterface();
            paramMetaData.setString(contentHash);
            callParams.set(0, paramMetaData);
            final Transaction txRegisterUser = contract.transact(tOpts, "updateMetaData", callParams);
            transactionHash = txRegisterUser.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex(), transactionHash.getHex(), DatabaseHelper.TX_ACTION_ID_UPDATE_USER_PIC, contentHash, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txRegisterUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_UPDATE_USER_PIC_PENDING_CONFIRMATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handlePublishUserContent(ContentItem content, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        String addressString = PrefUtils.getSelectedAccountAddress(getApplicationContext());
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract userContract = Geth.bindContract(
                    new Address(EthereumConstants.USER_CONTENT_REGISTER_ADDRESS_RINKEBY),
                    USER_CONTENT_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(addressString);


            CallOpts callOpts = Geth.newCallOpts();
            callOpts.setContext(mContext);
            Interfaces callData;
            Interfaces returnData;

            //Find number of articles
            callData = Geth.newInterfaces(1);
            Interface addressParameter = Geth.newInterface();
            addressParameter.setAddress(Geth.newAddressFromHex(addressString));
            callData.set(0, addressParameter);

            returnData = Geth.newInterfaces(3);
            Interface userNameParam = Geth.newInterface();
            userNameParam.setDefaultString();
            Interface metaParam = Geth.newInterface();
            metaParam.setDefaultString();
            Interface numContentParam = Geth.newInterface();
            numContentParam.setDefaultBigInt();
            returnData.set(0, userNameParam);
            returnData.set(1, metaParam);
            returnData.set(2, numContentParam);

            userContract.call(callOpts, returnData, "userIndex", callData);
            long numContent = returnData.get(2).getBigInt().getInt64();
            content.userContentIndex = (int)numContent;


            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            tOpts.setValue(new BigInt(0));
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // if (content.primaryImageUrl != "empty")
//            final String imageHash = new IPFS().getAdd().file(new File(content.primaryImageUrl)).getHash();
//            content.primaryImageUrl = imageHash;
            String contentJson = convertContentItemToJSON(content);

            final String contentHash = new IPFS().getAdd().string(contentJson).getHash();

            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File ethercircusFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ethercircus");
                if (!ethercircusFolder.exists()) ethercircusFolder.mkdirs();
                File addressFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + "ethercircus", addressString);
                if (!addressFolder.exists()) addressFolder.mkdirs();
                File contentFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/ethercircus/" + addressString + "/", "content_" + numContent);
                if (!contentFolder.exists()) contentFolder.mkdirs();
                FileOutputStream fo = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "ethercircus" + File.separator + addressString + File.separator + "content_" + numContent + File.separator + "content.json");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
                outputStreamWriter.write(contentJson);
                outputStreamWriter.close();
                fo.close();
            }

            //publish to user feed
            Interfaces callParams = Geth.newInterfaces(1);
            Interface paramContentHash = Geth.newInterface();
            paramContentHash.setString(contentHash);
            callParams.set(0, paramContentHash);
            final Transaction txPublishContent = userContract.transact(tOpts, "publishContent", callParams);

            transactionHash = txPublishContent.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex(), txPublishContent.getHash().getHex(), DatabaseHelper.TX_ACTION_ID_PUBLISH_USER_CONTENT,
                    contentJson, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txPublishContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_PUBLISH_USER_CONTENT_PENDING_CONFIRMATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handlePublishUserContentToPublication(int whichPublication, int index, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract publicationContract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            tOpts.setValue(new BigInt(0));
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // publish to slush pile
            Interfaces callParams = Geth.newInterfaces(2);
            Interface paramWhichPublication = Geth.newInterface();
            paramWhichPublication.setBigInt(new BigInt(whichPublication));
            Interface paramUserContentIndex = Geth.newInterface();
            paramUserContentIndex.setBigInt(Geth.newBigInt(index));
            callParams.set(0, paramWhichPublication);
            callParams.set(1, paramUserContentIndex);
            final Transaction txPublishToPublication = publicationContract.transact(tOpts, "publishContent", callParams);

            transactionHash = txPublishToPublication.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_PUBLISH_TO_PUBLICATION, "" + index, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txPublishToPublication);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_PUBLISH_USER_CONTENT_TO_PUBLICATION_PENDING_CONFIRMATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }



    private void handleCreatePublication(String name, String meta, String minCost, String adminPay, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract publicationContract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setGasLimit(1000000);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            tOpts.setValue(new BigInt(0));
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // publish to slush pile
            Interfaces callParams = Geth.newInterfaces(4);

            Interface paramName = Geth.newInterface();
            Interface paramMetaData = Geth.newInterface();
            Interface paramMinUpvoteCost = Geth.newInterface();
            Interface paramAdminPaymentPercentage = Geth.newInterface();

            paramName.setString(name);
            paramMetaData.setString(meta);
            paramMinUpvoteCost.setBigInt(Geth.newBigInt(Long.valueOf(minCost)));
            long adminPayLong = Long.valueOf(adminPay);
            paramAdminPaymentPercentage.setUint8(Geth.newBigInt(adminPayLong));

            callParams.set(0, paramName);
            callParams.set(1, paramMetaData);
            callParams.set(2, paramMinUpvoteCost);
            callParams.set(3, paramAdminPaymentPercentage);

            final Transaction txCreatePublication = publicationContract.transact(tOpts, "createPublication", callParams);

            transactionHash = txCreatePublication.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_CREATE_PUBLICATION, name, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txCreatePublication);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_CREATE_PUBLICATION_PENDING_CONFIRMATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handleSupportPost(String amountString, String comment, long whichPublication, long contentIndex, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract publicationContract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setGasLimit(1000000);
            BigInt supportAmount = new BigInt(Convert.toWei(amountString, Convert.Unit.ETHER).longValue());
            tOpts.setValue(supportAmount);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // call supportPost
            Interfaces callParams = Geth.newInterfaces(3);

            Interface paramWhichPub = Geth.newInterface();
            Interface paramPostIndex = Geth.newInterface();
            Interface paramComment = Geth.newInterface();

            paramWhichPub.setBigInt(new BigInt(whichPublication));
            paramPostIndex.setBigInt(new BigInt(contentIndex));
            paramComment.setString(comment);

            callParams.set(0, paramWhichPub);
            callParams.set(1, paramPostIndex);
            callParams.set(2, paramComment);

            final Transaction txSupportPost = publicationContract.transact(tOpts, "supportPost", callParams);

            transactionHash = txSupportPost.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_SUPPORT_POST, "sent " + supportAmount.getInt64() + ": " +  comment, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txSupportPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_SUPPORT_POST_SENT);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handleWithdrawAuthorClaim(int whichPublication, String selectedUserForWithdraw, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract publicationContract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setGasLimit(1000000);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // call supportPost
            Interfaces callParams = Geth.newInterfaces(1);

            Interface paramWhichPub = Geth.newInterface();

            paramWhichPub.setBigInt(new BigInt(whichPublication));

            callParams.set(0, paramWhichPub);

            final Transaction txWithdrawAuthorClaim = publicationContract.transact(tOpts, "withdrawAuthorClaim", callParams);

            transactionHash = txWithdrawAuthorClaim.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_WITHDRAW_AUTHOR_CLAIM, "withdraw from pubID: " + whichPublication, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txWithdrawAuthorClaim);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_WITHDRAW_AUTHOR_CLAIM_SENT);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handleWithdrawAdminClaim(int whichPublication, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract publicationContract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setGasLimit(1000000);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // call supportPost
            Interfaces callParams = Geth.newInterfaces(1);

            Interface paramWhichPub = Geth.newInterface();

            paramWhichPub.setBigInt(new BigInt(whichPublication));

            callParams.set(0, paramWhichPub);

            final Transaction txWithdrawAdminClaim = publicationContract.transact(tOpts, "withdrawAdminClaim", callParams);

            transactionHash = txWithdrawAdminClaim.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_WITHDRAW_ADMIN_CLAIM, "withdraw from pubID: " + whichPublication, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txWithdrawAdminClaim);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_WITHDRAW_ADMIN_CLAIM_SENT);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }

    private void handlePermissionAuthor(int whichPublication, String whichAuthor, boolean permissionEnabled, final String password) {
        Hash transactionHash = null;
        DBEthereumTransaction ethereumTransaction = null;
        try {
            final KeyStore mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);

            BoundContract publicationContract = Geth.bindContract(
                    new Address(EthereumConstants.PUBLICATION_REGISTER_ADDRESS_RINKEBY),
                    PUBLICATION_REGISTER_ABI, mEthereumClient);

            Address address = Geth.newAddressFromHex(PrefUtils.getSelectedAccountAddress(getBaseContext()));

            TransactOpts tOpts = new TransactOpts();
            tOpts.setContext(mContext);
            tOpts.setFrom(address);
            tOpts.setGasLimit(1000000);
            tOpts.setSigner(new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                    Account account = mKeyStore.getAccounts().get(PrefUtils.getSelectedAccountNum(getBaseContext()));
                    mKeyStore.unlock(account, password);
                    Transaction signed = mKeyStore.signTx(account, transaction, new BigInt(4));
                    mKeyStore.lock(account.getAddress());
                    return signed;
                }
            });
            long noncePending  = mEthereumClient.getPendingNonceAt(mContext, address);
            tOpts.setNonce(noncePending);

            // call supportPost
            Interfaces callParams = Geth.newInterfaces(3);

            Interface paramWhichPub = Geth.newInterface();
            Interface paramWhichAuthor = Geth.newInterface();
            Interface paramWhichPermissionEnabled = Geth.newInterface();

            paramWhichPub.setBigInt(new BigInt(whichPublication));
            paramWhichAuthor.setAddress(new Address(whichAuthor));
            paramWhichPermissionEnabled.setBool(permissionEnabled);

            callParams.set(0, paramWhichPub);
            callParams.set(1, paramWhichAuthor);
            callParams.set(2, paramWhichPermissionEnabled);

            final Transaction txPermissionAuthor = publicationContract.transact(tOpts, "permissionAuthor", callParams);

            transactionHash = txPermissionAuthor.getHash();
            ethereumTransaction = new DBEthereumTransaction(address.getHex().toString(), transactionHash.getHex().toString(), DatabaseHelper.TX_ACTION_ID_PERMISSION_AUTHOR, whichAuthor + "enabled: " + permissionEnabled, System.currentTimeMillis(), 0, false, 0);
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            helper.saveTransactionInfo(ethereumTransaction);
            mEthereumClient.sendTransaction(mContext, txPermissionAuthor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UI_PERMISSION_AUTHOR_SENT);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(EthereumClientService.this);
        bm.sendBroadcast(intent);
        pollForTransactionConfirmation(transactionHash, ethereumTransaction);
    }


    private void pollForTransactionConfirmation(Hash txHash, DBEthereumTransaction tx) {
        long timestamp = System.currentTimeMillis();
        Receipt receipt = null;
        if (tx != null && txHash != null) {
            try {
                receipt = mEthereumClient.getTransactionReceipt(mContext, txHash);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (receipt == null && System.currentTimeMillis() - timestamp < 60000) {
                try {
                    Thread.sleep(2000);
                    receipt = mEthereumClient.getTransactionReceipt(mContext, txHash);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (receipt != null) { //receipt found
                try {
                    Transaction ethTx = null;
                    int attempts = 0;
                    long currentBlockNumber = mBlockNumber;
                    while (ethTx == null && attempts <= 20) {
                        Block b = mEthereumClient.getBlockByNumber(mContext, currentBlockNumber - attempts);
                        ethTx = b.getTransaction(txHash);
                        if (ethTx != null) {
                            tx.txTimestamp = b.getTime();
                            tx.blockNumber = currentBlockNumber - attempts;
                            tx.confirmed = true;
                            tx.gasCost = ethTx.getGas();
                        }
                        attempts++;
                    }
                    DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
                    helper.saveTransactionInfo(tx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String convertContentItemToJSON(ContentItem contentItem) {
        Gson gson = new Gson();
        String json = gson.toJson(contentItem);
        return json;
    }

    private ContentItem convertJsonToContentItem(String json) {
        if (json.equals("CONTENT CURRENTLY UNAVAILABLE")) {
            return null;
        }
        Gson gson = new Gson();
        ContentItem contentItem = gson.fromJson(json, ContentItem.class);
        return contentItem;
    }

}
